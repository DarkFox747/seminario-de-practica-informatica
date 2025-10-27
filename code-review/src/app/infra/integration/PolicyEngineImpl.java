package app.infra.integration;

import app.domain.entity.Finding;
import app.domain.entity.SeverityPolicy;
import app.domain.port.PolicyEngine;
import app.domain.value.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Policy engine implementation using strategy pattern.
 * Applies classification rules from policy JSON to findings.
 */
public class PolicyEngineImpl implements PolicyEngine {
    
    @Override
    public void applyPolicy(Finding finding, SeverityPolicy policy) {
        if (policy == null || policy.getRulesJson() == null) {
            // No policy, keep original severity
            return;
        }
        
        List<PolicyRule> rules = parseRules(policy.getRulesJson());
        
        for (PolicyRule rule : rules) {
            if (matches(finding, rule)) {
                applySeverityAction(finding, rule);
                break; // Apply first matching rule only
            }
        }
    }
    
    @Override
    public boolean validatePolicyRules(String rulesJson) {
        try {
            List<PolicyRule> rules = parseRules(rulesJson);
            return !rules.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean matches(Finding finding, PolicyRule rule) {
        // Check rule pattern
        if (rule.rulePattern != null && finding.getRuleId() != null) {
            Pattern pattern = Pattern.compile(rule.rulePattern);
            if (!pattern.matcher(finding.getRuleId()).matches()) {
                return false;
            }
        }
        
        // Check category pattern
        if (rule.categoryPattern != null && finding.getCategory() != null) {
            Pattern pattern = Pattern.compile(rule.categoryPattern);
            if (!pattern.matcher(finding.getCategory()).matches()) {
                return false;
            }
        }
        
        // Check message pattern
        if (rule.messagePattern != null && finding.getMessage() != null) {
            Pattern pattern = Pattern.compile(rule.messagePattern);
            if (!pattern.matcher(finding.getMessage()).matches()) {
                return false;
            }
        }
        
        // Check severity threshold
        if (rule.severityThreshold != null) {
            Severity threshold = Severity.valueOf(rule.severityThreshold);
            if (compareSeverity(finding.getSeverityRaw(), threshold) < 0) {
                return false;
            }
        }
        
        return true;
    }
    
    private void applySeverityAction(Finding finding, PolicyRule rule) {
        switch (rule.action.toLowerCase()) {
            case "upgrade":
                if (rule.targetSeverity != null) {
                    Severity target = Severity.valueOf(rule.targetSeverity);
                    if (compareSeverity(target, finding.getSeverityFinal()) > 0) {
                        finding.setSeverityFinal(target);
                    }
                }
                break;
                
            case "downgrade":
                if (rule.targetSeverity != null) {
                    Severity target = Severity.valueOf(rule.targetSeverity);
                    if (compareSeverity(target, finding.getSeverityFinal()) < 0) {
                        finding.setSeverityFinal(target);
                    }
                }
                break;
                
            case "keep":
                // Keep original severity
                break;
                
            case "set":
                if (rule.targetSeverity != null) {
                    finding.setSeverityFinal(Severity.valueOf(rule.targetSeverity));
                }
                break;
        }
    }
    
    /**
     * Compare severity levels (higher severity = higher value).
     * CRITICAL > HIGH > MEDIUM > LOW > INFO
     */
    private int compareSeverity(Severity s1, Severity s2) {
        return getSeverityValue(s1) - getSeverityValue(s2);
    }
    
    private int getSeverityValue(Severity severity) {
        switch (severity) {
            case CRITICAL: return 5;
            case HIGH: return 4;
            case MEDIUM: return 3;
            case LOW: return 2;
            case INFO: return 1;
            default: return 0;
        }
    }
    
    /**
     * Parse policy rules from JSON (simple parsing without external libs).
     */
    private List<PolicyRule> parseRules(String json) {
        List<PolicyRule> rules = new ArrayList<>();
        
        try {
            // Extract rules array
            int rulesStart = json.indexOf("\"rules\"");
            if (rulesStart < 0) return rules;
            
            int arrayStart = json.indexOf('[', rulesStart);
            int arrayEnd = json.lastIndexOf(']');
            
            if (arrayStart < 0 || arrayEnd < 0) return rules;
            
            String rulesArray = json.substring(arrayStart + 1, arrayEnd);
            
            // Split by rule objects
            String[] ruleObjects = rulesArray.split("\\},\\s*\\{");
            
            for (String ruleObj : ruleObjects) {
                ruleObj = ruleObj.replace("{", "").replace("}", "").trim();
                
                PolicyRule rule = new PolicyRule();
                rule.rulePattern = extractJsonValue(ruleObj, "rulePattern");
                rule.categoryPattern = extractJsonValue(ruleObj, "categoryPattern");
                rule.messagePattern = extractJsonValue(ruleObj, "messagePattern");
                rule.severityThreshold = extractJsonValue(ruleObj, "severityThreshold");
                rule.action = extractJsonValue(ruleObj, "action");
                rule.targetSeverity = extractJsonValue(ruleObj, "targetSeverity");
                
                rules.add(rule);
            }
        } catch (Exception e) {
            System.err.println("Error parsing policy rules: " + e.getMessage());
        }
        
        return rules;
    }
    
    private String extractJsonValue(String obj, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"";
        int start = obj.indexOf(pattern);
        if (start < 0) return null;
        
        start += pattern.length();
        int end = obj.indexOf('"', start);
        if (end < 0) return null;
        
        return obj.substring(start, end);
    }
    
    /**
     * Internal class to hold policy rule data.
     */
    private static class PolicyRule {
        String rulePattern;
        String categoryPattern;
        String messagePattern;
        String severityThreshold;
        String action;
        String targetSeverity;
    }
}
