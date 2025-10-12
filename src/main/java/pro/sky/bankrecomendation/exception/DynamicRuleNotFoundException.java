package pro.sky.bankrecomendation.exception;

import java.util.UUID;

public class DynamicRuleNotFoundException extends RuntimeException {
    public DynamicRuleNotFoundException(String message) {
        super(message);
    }

    public DynamicRuleNotFoundException(UUID ruleId) {
        super("Dynamic rule not found with ID: " + ruleId);
    }
}