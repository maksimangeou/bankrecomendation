package pro.sky.bankrecomendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleRequest;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleResponse;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRulesListResponse;
import pro.sky.bankrecomendation.exception.DynamicRuleNotFoundException;
import pro.sky.bankrecomendation.exception.DynamicRuleValidationException;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.repository.dynamic.DynamicRuleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DynamicRuleService {

    private static final Logger log = LoggerFactory.getLogger(DynamicRuleService.class);

    private final DynamicRuleRepository dynamicRuleRepository;
    private final DynamicRuleMapper mapper;

    public DynamicRuleService(DynamicRuleRepository dynamicRuleRepository,
                              DynamicRuleMapper mapper) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.mapper = mapper;
    }

    public DynamicRuleResponse createRule(DynamicRuleRequest request) {
        validateRuleRequest(request);
        log.debug("Creating new dynamic rule for product: {}", request.getProductName());

        DynamicRule rule = mapper.toEntity(request);
        DynamicRule saved = dynamicRuleRepository.save(rule);

        log.info("Dynamic rule created successfully. Rule ID: {}, Product: {}",
                saved.getId(), saved.getProductName());

        return mapper.toResponse(saved);
    }

    public DynamicRulesListResponse getAllRules() {
        log.debug("Retrieving all dynamic rules");

        List<DynamicRule> rules = dynamicRuleRepository.findAll();
        List<DynamicRuleResponse> responseList = rules.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Retrieved {} dynamic rules", responseList.size());
        return new DynamicRulesListResponse(responseList);
    }

    public void deleteRule(UUID ruleId) {
        log.debug("Attempting to delete dynamic rule with ID: {}", ruleId);

        if (!dynamicRuleRepository.existsById(ruleId)) {
            throw new DynamicRuleNotFoundException(ruleId);
        }

        dynamicRuleRepository.deleteById(ruleId);
        log.info("Dynamic rule deleted successfully. Rule ID: {}", ruleId);
    }

    public DynamicRuleResponse getRuleById(UUID ruleId) {
        log.debug("Retrieving dynamic rule with ID: {}", ruleId);

        return dynamicRuleRepository.findById(ruleId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new DynamicRuleNotFoundException(ruleId));
    }

    public boolean ruleExists(UUID ruleId) {
        return dynamicRuleRepository.existsById(ruleId);
    }

    /**
     * Оценивает динамические правила для пользователя и возвращает рекомендации
     */
    public List<RecommendationDto> evaluateDynamicRules(UUID userId, UserFinancials metrics) {
        log.debug("Evaluating dynamic rules for user: {}", userId);

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

            log.debug("Found {} dynamic recommendations for user: {}", results.size(), userId);
            return results;

        } catch (Exception e) {
            log.error("Error evaluating dynamic rules for user: {}", userId, e);
            return List.of();
        }
    }

    private void validateRuleRequest(DynamicRuleRequest request) {
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new DynamicRuleValidationException("Product name cannot be empty");
        }
        if (request.getProductId() == null) {
            throw new DynamicRuleValidationException("Product ID cannot be null");
        }
        if (request.getProductText() == null || request.getProductText().trim().isEmpty()) {
            throw new DynamicRuleValidationException("Product text cannot be empty");
        }
        if (request.getRule() == null || request.getRule().isEmpty()) {
            throw new DynamicRuleValidationException("Rule conditions cannot be empty");
        }
    }
}