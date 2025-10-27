package app.domain.port;

import app.domain.entity.Finding;
import app.domain.entity.SeverityPolicy;

/**
 * Port for applying severity policies to findings.
 */
public interface PolicyEngine {
    
    /**
     * Apply policy rules to a finding and determine final severity.
     * 
     * @param finding The finding to classify
     * @param policy The policy to apply
     */
    void applyPolicy(Finding finding, SeverityPolicy policy);
    
    /**
     * Validate that policy rules JSON is well-formed.
     * 
     * @param rulesJson JSON with policy rules
     * @return true if valid
     */
    boolean validatePolicyRules(String rulesJson);
}
