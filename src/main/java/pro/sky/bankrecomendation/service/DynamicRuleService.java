package pro.sky.bankrecomendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.repository.dynamic.DynamicRuleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DynamicRuleService {

    private static final Logger log = LoggerFactory.getLogger(DynamicRuleService.class);

    private final DynamicRuleRepository dynamicRuleRepository;

    public DynamicRuleService(DynamicRuleRepository dynamicRuleRepository) {
        this.dynamicRuleRepository = dynamicRuleRepository;
    }

    public List<RecommendationDto> evaluateDynamicRules(UUID userId, UserFinancials metrics) {
        log.debug("Evaluating dynamic rules for user={}", userId);

        try {
            List<DynamicRule> rules = dynamicRuleRepository.findAll();
            List<RecommendationDto> results = new ArrayList<>();

            for (DynamicRule rule : rules) {
                // Временная логика - всегда возвращаем рекомендацию для тестирования
                // В реальной реализации здесь будет проверка условий
                results.add(new RecommendationDto(
                        rule.getProductId(),
                        rule.getProductName(),
                        rule.getProductText()
                ));
            }

            log.debug("Found {} dynamic recommendations for user={}", results.size(), userId);
            return results;

        } catch (Exception e) {
            log.error("Error evaluating dynamic rules for user={}", userId, e);
            return List.of();
        }
    }
}