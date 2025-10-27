package app.application.service;

import app.domain.entity.SeverityPolicy;
import app.domain.port.PolicyEngine;
import app.domain.port.SeverityPolicyRepository;
import app.domain.port.TxManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing severity policies.
 */
public class PolicyAdminService {
    
    private final SeverityPolicyRepository policyRepo;
    private final PolicyEngine policyEngine;
    private final TxManager txManager;
    
    public PolicyAdminService(
            SeverityPolicyRepository policyRepo,
            PolicyEngine policyEngine,
            TxManager txManager) {
        this.policyRepo = policyRepo;
        this.policyEngine = policyEngine;
        this.txManager = txManager;
    }
    
    /**
     * Create a new policy.
     */
    public SeverityPolicy createPolicy(String name, String description, String rulesJson, Long createdBy) 
            throws Exception {
        
        // Validate rules
        if (!policyEngine.validatePolicyRules(rulesJson)) {
            throw new Exception("Invalid policy rules JSON");
        }
        
        try {
            txManager.begin();
            
            SeverityPolicy policy = new SeverityPolicy(name, rulesJson);
            policy.setDescription(description);
            policy.setCreatedBy(createdBy);
            policy.setActive(false); // New policies start inactive
            
            SeverityPolicy saved = policyRepo.save(policy);
            txManager.commit();
            
            return saved;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Update existing policy (creates new version).
     */
    public SeverityPolicy updatePolicy(Long policyId, String rulesJson) throws Exception {
        
        // Validate rules
        if (!policyEngine.validatePolicyRules(rulesJson)) {
            throw new Exception("Invalid policy rules JSON");
        }
        
        try {
            txManager.begin();
            
            SeverityPolicy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found: " + policyId));
            
            policy.setRulesJson(rulesJson);
            policy.setUpdatedAt(LocalDateTime.now());
            policy.incrementVersion();
            
            SeverityPolicy updated = policyRepo.save(policy);
            txManager.commit();
            
            return updated;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Activate a policy (deactivates others).
     */
    public void activatePolicy(Long policyId) throws Exception {
        try {
            txManager.begin();
            
            // Deactivate current active policy
            SeverityPolicy currentActive = policyRepo.findActivePolicy().orElse(null);
            if (currentActive != null) {
                currentActive.setActive(false);
                policyRepo.save(currentActive);
            }
            
            // Activate new policy
            SeverityPolicy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found: " + policyId));
            
            policy.setActive(true);
            policyRepo.save(policy);
            
            txManager.commit();
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Get all policies.
     */
    public List<SeverityPolicy> getAllPolicies() throws Exception {
        try {
            txManager.begin();
            List<SeverityPolicy> policies = policyRepo.findAll();
            txManager.commit();
            return policies;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Get active policy.
     */
    public SeverityPolicy getActivePolicy() throws Exception {
        try {
            txManager.begin();
            SeverityPolicy policy = policyRepo.findActivePolicy()
                .orElse(null);
            txManager.commit();
            return policy;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Get policy by ID.
     */
    public SeverityPolicy getPolicy(Long policyId) throws Exception {
        try {
            txManager.begin();
            SeverityPolicy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found: " + policyId));
            txManager.commit();
            return policy;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Get policy versions by name.
     */
    public List<SeverityPolicy> getPolicyVersions(String name) throws Exception {
        try {
            txManager.begin();
            List<SeverityPolicy> policies = policyRepo.findByName(name);
            txManager.commit();
            return policies;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
}
