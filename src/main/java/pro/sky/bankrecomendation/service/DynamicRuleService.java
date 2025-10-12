package pro.sky.bankrecomendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.dto.dynamic.RuleConditionDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.model.dynamic.QueryType;
import pro.sky.bankrecomendation.repository.RecommendationRepository;
import pro.sky.bankrecomendation.repository.dynamic.DynamicRuleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DynamicRuleService {

    private static final Logger log = LoggerFactory.getLogger(DynamicRuleService.class);

    private final DynamicRuleRepository dynamicRuleRepository;
    private final RecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;

    public DynamicRuleService(DynamicRuleRepository dynamicRuleRepository,
                              RecommendationRepository recommendationRepository,
                              ObjectMapper objectMapper) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.recommendationRepository = recommendationRepository;
        this.objectMapper = objectMapper;
    }

    public List<RecommendationDto> evaluateDynamicRules(UUID userId, UserFinancials metrics) {
        log.debug("Evaluating dynamic rules for user={}", userId);

        try {
            List<DynamicRule> rules = dynamicRuleRepository.findAll();
            List<RecommendationDto> results = new ArrayList<>();

            for (DynamicRule rule : rules) {
                if (evaluateRule(userId, rule, metrics)) {
                    results.add(new RecommendationDto(
                            rule.getProductId(),
                            rule.getProductName(),
                            rule.getProductText()
                    ));
                    log.debug("Rule {} passed for user {}", rule.getId(), userId);
                } else {
                    log.debug("Rule {} failed for user {}", rule.getId(), userId);
                }
            }

            log.debug("Found {} dynamic recommendations for user={}", results.size(), userId);
            return results;

        } catch (Exception e) {
            log.error("Error evaluating dynamic rules for user={}", userId, e);
            return List.of();
        }
    }

    private boolean evaluateRule(UUID userId, DynamicRule rule, UserFinancials metrics) {
        try {
            List<RuleConditionDto> conditions = objectMapper.readValue(
                    rule.getRuleJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RuleConditionDto.class)
            );

            for (RuleConditionDto condition : conditions) {
                boolean conditionResult = evaluateCondition(userId, condition, metrics);
                boolean finalResult = condition.isNegate() ? !conditionResult : conditionResult;

                if (!finalResult) {
                    log.debug("Condition failed for rule {}: {}", rule.getId(), condition);
                    return false;
                }
            }
            return true;
        } catch (JsonProcessingException e) {
            log.error("Error parsing rule JSON for rule {}", rule.getId(), e);
            return false;
        } catch (Exception e) {
            log.error("Error evaluating rule {}", rule.getId(), e);
            return false;
        }
    }

    private boolean evaluateCondition(UUID userId, RuleConditionDto condition, UserFinancials metrics) {
        try {
            QueryType queryType = condition.getQuery();
            List<String> arguments = condition.getArguments();

            return switch (queryType) {
                case USER_OF -> evaluateUserOf(userId, arguments);
                case ACTIVE_USER_OF -> evaluateActiveUserOf(userId, arguments);
                case TRANSACTION_SUM_COMPARE -> evaluateTransactionSumCompare(userId, arguments);
                case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW -> evaluateDepositWithdrawCompare(userId, arguments);
            };
        } catch (Exception e) {
            log.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }

    private boolean evaluateUserOf(UUID userId, List<String> arguments) {
        if (arguments.size() < 1) {
            log.warn("USER_OF condition requires 1 argument");
            return false;
        }
        String productType = arguments.get(0);
        return recommendationRepository.isUserOfProductType(userId, productType);
    }

    private boolean evaluateActiveUserOf(UUID userId, List<String> arguments) {
        if (arguments.size() < 1) {
            log.warn("ACTIVE_USER_OF condition requires 1 argument");
            return false;
        }
        String productType = arguments.get(0);
        return recommendationRepository.isActiveUserOfProductType(userId, productType);
    }

    private boolean evaluateTransactionSumCompare(UUID userId, List<String> arguments) {
        if (arguments.size() < 4) {
            log.warn("TRANSACTION_SUM_COMPARE condition requires 4 arguments");
            return false;
        }

        String productType = arguments.get(0);
        String transactionType = arguments.get(1);
        String operator = arguments.get(2);
        double value;

        try {
            value = Double.parseDouble(arguments.get(3));
        } catch (NumberFormatException e) {
            log.warn("Invalid number format in TRANSACTION_SUM_COMPARE: {}", arguments.get(3));
            return false;
        }

        Double sum = recommendationRepository.getTransactionSum(userId, productType, transactionType);
        return compareValues(sum, operator, value);
    }

    private boolean evaluateDepositWithdrawCompare(UUID userId, List<String> arguments) {
        if (arguments.size() < 2) {
            log.warn("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW condition requires 2 arguments");
            return false;
        }

        String productType = arguments.get(0);
        String operator = arguments.get(1);

        Double depositSum = recommendationRepository.getTransactionSum(userId, productType, "DEPOSIT");
        Double withdrawSum = recommendationRepository.getTransactionSum(userId, productType, "WITHDRAW");

        return compareValues(depositSum, operator, withdrawSum);
    }

    private boolean compareValues(Double value1, String operator, Double value2) {
        if (value1 == null || value2 == null) {
            return false;
        }

        return switch (operator) {
            case ">" -> value1 > value2;
            case "<" -> value1 < value2;
            case "=" -> Math.abs(value1 - value2) < 0.001; // учет погрешности для double
            case ">=" -> value1 >= value2;
            case "<=" -> value1 <= value2;
            default -> {
                log.warn("Unknown operator: {}", operator);
                yield false;
            }
        };
    }
}