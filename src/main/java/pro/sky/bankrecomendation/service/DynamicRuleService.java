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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DynamicRuleService {

    private static final Logger log = LoggerFactory.getLogger(DynamicRuleService.class);

    private final DynamicRuleRepository dynamicRuleRepository;
    private final RecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;

    public DynamicRuleService(DynamicRuleRepository dynamicRuleRepository,
                              RecommendationRepository recommendationRepository) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.recommendationRepository = recommendationRepository;
        this.objectMapper = new ObjectMapper();
    }

    public List<RecommendationDto> evaluateDynamicRules(UUID userId, UserFinancials metrics) {
        log.debug("Evaluating dynamic rules for user={}", userId);

        List<DynamicRule> rules = dynamicRuleRepository.findAll();

        List<RecommendationDto> results = rules.stream()
                .filter(rule -> evaluateRule(userId, metrics, rule))
                .map(rule -> new RecommendationDto(
                        rule.getProductId(),
                        rule.getProductName(),
                        rule.getProductText()
                ))
                .collect(Collectors.toList());

        log.debug("Found {} dynamic recommendations for user={}", results.size(), userId);
        return results;
    }

    private boolean evaluateRule(UUID userId, UserFinancials metrics, DynamicRule rule) {
        try {
            List<RuleConditionDto> conditions = objectMapper.readValue(
                    rule.getRuleJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RuleConditionDto.class)
            );

            boolean result = conditions.stream()
                    .allMatch(condition -> evaluateCondition(userId, metrics, condition));

            log.debug("Rule evaluation for user={}, rule={}: {}", userId, rule.getProductName(), result);
            return result;

        } catch (JsonProcessingException e) {
            log.error("Error parsing rule JSON for rule={}", rule.getId(), e);
            return false;
        }
    }

    private boolean evaluateCondition(UUID userId, UserFinancials metrics, RuleConditionDto condition) {
        boolean result;

        try {
            result = switch (condition.getQuery()) {
                case USER_OF -> evaluateUserOf(userId, condition.getArguments().get(0));
                case ACTIVE_USER_OF -> evaluateActiveUserOf(userId, condition.getArguments().get(0));
                case TRANSACTION_SUM_COMPARE -> evaluateTransactionSumCompare(userId, condition.getArguments());
                case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW -> evaluateDepositWithdrawCompare(userId, condition.getArguments());
            };
        } catch (Exception e) {
            log.error("Error evaluating condition for user={}, condition={}", userId, condition.getQuery(), e);
            result = false;
        }

        boolean finalResult = condition.isNegate() != result;
        log.debug("Condition evaluation for user={}, query={}, negate={}: {} -> {}",
                userId, condition.getQuery(), condition.isNegate(), result, finalResult);

        return finalResult;
    }

    private boolean evaluateUserOf(UUID userId, String productType) {
        return recommendationRepository.isUserOfProductType(userId, productType);
    }

    private boolean evaluateActiveUserOf(UUID userId, String productType) {
        return recommendationRepository.isActiveUserOfProductType(userId, productType);
    }

    private boolean evaluateTransactionSumCompare(UUID userId, List<String> arguments) {
        if (arguments.size() < 4) {
            log.error("Invalid arguments for TRANSACTION_SUM_COMPARE: {}", arguments);
            return false;
        }

        String productType = arguments.get(0);
        String transactionType = arguments.get(1);
        String operator = arguments.get(2);
        double compareValue;

        try {
            compareValue = Double.parseDouble(arguments.get(3));
        } catch (NumberFormatException e) {
            log.error("Invalid compare value: {}", arguments.get(3));
            return false;
        }

        Double sum = recommendationRepository.getTransactionSum(userId, productType, transactionType);
        return compareValues(sum, operator, compareValue);
    }

    private boolean evaluateDepositWithdrawCompare(UUID userId, List<String> arguments) {
        if (arguments.size() < 2) {
            log.error("Invalid arguments for TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW: {}", arguments);
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
            case "=" -> Math.abs(value1 - value2) < 0.001;
            case ">=" -> value1 >= value2;
            case "<=" -> value1 <= value2;
            default -> {
                log.error("Unknown operator: {}", operator);
                yield false;
            }
        };
    }
}