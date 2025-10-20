package pro.sky.bankrecomendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.dynamic.RuleConditionDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.model.dynamic.QueryType;
import pro.sky.bankrecomendation.repository.RecommendationRepository;

import java.util.List;
import java.util.UUID;

@Component
public class DynamicRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(DynamicRuleEngine.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RecommendationRepository recommendationRepository;

    public DynamicRuleEngine(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    public boolean evaluateRule(DynamicRule rule, UUID userId, UserFinancials metrics) {
        try {
            List<RuleConditionDto> conditions = objectMapper.readValue(
                    rule.getRuleJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RuleConditionDto.class)
            );

            boolean result = true;
            for (RuleConditionDto condition : conditions) {
                boolean conditionResult = evaluateCondition(condition, userId, metrics);
                if (condition.isNegate()) {
                    conditionResult = !conditionResult;
                }
                result = result && conditionResult;

                if (!result) break;
            }

            return result;

        } catch (JsonProcessingException e) {
            log.error("Error parsing rule conditions for rule {}: {}", rule.getId(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error evaluating rule {}: {}", rule.getId(), e.getMessage());
            return false;
        }
    }

    private boolean evaluateCondition(RuleConditionDto condition, UUID userId, UserFinancials metrics) {
        QueryType queryType = condition.getQuery();
        List<String> arguments = condition.getArguments();

        switch (queryType) {
            case USER_OF:
                return arguments.size() >= 1 &&
                       recommendationRepository.isUserOfProductType(userId, arguments.get(0));

            case ACTIVE_USER_OF:
                return arguments.size() >= 1 &&
                       recommendationRepository.isActiveUserOfProductType(userId, arguments.get(0));

            case TRANSACTION_SUM_COMPARE:
                if (arguments.size() >= 3) {
                    Double actualSum = recommendationRepository.getTransactionSum(
                            userId, arguments.get(0), arguments.get(1));
                    Double expectedSum = Double.parseDouble(arguments.get(2));
                    return actualSum >= expectedSum;
                }
                return false;

            case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW:
                if (arguments.size() >= 2) {
                    Double depositSum = recommendationRepository.getTransactionSum(
                            userId, arguments.get(0), "DEPOSIT");
                    Double withdrawSum = recommendationRepository.getTransactionSum(
                            userId, arguments.get(0), "WITHDRAW");
                    Double expectedRatio = Double.parseDouble(arguments.get(1));
                    return depositSum > withdrawSum * expectedRatio;
                }
                return false;

            default:
                log.warn("Unknown query type: {}", queryType);
                return false;
        }
    }
}