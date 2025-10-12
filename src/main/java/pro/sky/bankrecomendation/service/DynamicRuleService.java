package pro.sky.bankrecomendation.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.model.dynamic.QueryType;
import pro.sky.bankrecomendation.model.dynamic.RuleCondition;
import pro.sky.bankrecomendation.repository.RecommendationRepository;
import pro.sky.bankrecomendation.repository.dynamic.DynamicRuleRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DynamicRuleService {

    private final DynamicRuleRepository dynamicRuleRepository;
    private final RecommendationRepository recommendationRepository;

    // Кеши для различных типов запросов
    private final Cache<String, Boolean> userOfCache;
    private final Cache<String, Boolean> activeUserOfCache;
    private final Cache<String, Double> transactionSumCache;
    private final Cache<String, Boolean> depositWithdrawCompareCache;

    public DynamicRuleService(DynamicRuleRepository dynamicRuleRepository,
                              RecommendationRepository recommendationRepository) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.recommendationRepository = recommendationRepository;

        this.userOfCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.activeUserOfCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.transactionSumCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.depositWithdrawCompareCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public List<RecommendationDto> evaluateDynamicRules(UUID userId, UserFinancials metrics) {
        List<DynamicRule> rules = dynamicRuleRepository.findAll();

        return rules.stream()
                .filter(rule -> evaluateRule(userId, metrics, rule))
                .map(rule -> new RecommendationDto(
                        rule.getProductId(),
                        rule.getProductName(),
                        rule.getProductText()
                ))
                .collect(Collectors.toList());
    }

    private boolean evaluateRule(UUID userId, UserFinancials metrics, DynamicRule rule) {
        return rule.getConditions().stream()
                .allMatch(condition -> evaluateCondition(userId, metrics, condition));
    }

    private boolean evaluateCondition(UUID userId, UserFinancials metrics, RuleCondition condition) {
        boolean result = switch (condition.getQuery()) {
            case USER_OF -> evaluateUserOf(userId, condition.getArguments().get(0));
            case ACTIVE_USER_OF -> evaluateActiveUserOf(userId, condition.getArguments().get(0));
            case TRANSACTION_SUM_COMPARE -> evaluateTransactionSumCompare(userId, condition.getArguments());
            case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW -> evaluateDepositWithdrawCompare(userId, condition.getArguments());
        };

        return condition.isNegate() != result;
    }

    private boolean evaluateUserOf(UUID userId, String productType) {
        String cacheKey = userId + ":" + productType;
        return userOfCache.get(cacheKey, key -> {
            // Реализация проверки использования продукта
            return recommendationRepository.isUserOfProductType(userId, productType);
        });
    }

    private boolean evaluateActiveUserOf(UUID userId, String productType) {
        String cacheKey = userId + ":" + productType;
        return activeUserOfCache.gevt(cacheKey, key -> {
            // Реализация проверки активного использования
            return recommendationRepository.isActiveUserOfProductType(userId, productType);
        });
    }

    private boolean evaluateTransactionSumCompare(UUID userId, List<String> arguments) {
        String cacheKey = userId + ":" + String.join(":", arguments);
        Double sum = transactionSumCache.get(cacheKey, key ->
                recommendationRepository.getTransactionSum(userId, arguments.get(0), arguments.get(1))
        );

        return compareValues(sum, arguments.get(2), Double.parseDouble(arguments.get(3)));
    }

    private boolean evaluateDepositWithdrawCompare(UUID userId, List<String> arguments) {
        String cacheKey = userId + ":" + String.join(":", arguments);
        return depositWithdrawCompareCache.get(cacheKey, key -> {
            Double depositSum = recommendationRepository.getTransactionSum(userId, arguments.get(0), "DEPOSIT");
            Double withdrawSum = recommendationRepository.getTransactionSum(userId, arguments.get(0), "WITHDRAW");
            return compareValues(depositSum, arguments.get(1), withdrawSum);
        });
    }

    private boolean compareValues(Double value1, String operator, Double value2) {
        return switch (operator) {
            case ">" -> value1 > value2;
            case "<" -> value1 < value2;
            case "=" -> value1.equals(value2);
            case ">=" -> value1 >= value2;
            case "<=" -> value1 <= value2;
            default -> false;
        };
    }
}