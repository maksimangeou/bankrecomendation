package pro.sky.bankrecomendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.dto.dynamic.*;
import pro.sky.bankrecomendation.exception.DynamicRuleNotFoundException;
import pro.sky.bankrecomendation.exception.DynamicRuleValidationException;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.model.dynamic.RuleStatistics;
import pro.sky.bankrecomendation.repository.dynamic.DynamicRuleRepository;
import pro.sky.bankrecomendation.repository.dynamic.RuleStatisticsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DynamicRuleService {

    private static final Logger log = LoggerFactory.getLogger(DynamicRuleService.class);

    private final DynamicRuleRepository dynamicRuleRepository;
    private final RuleStatisticsRepository statisticsRepository;
    private final DynamicRuleMapper mapper;
    private final DynamicRuleEngine dynamicRuleEngine;

    public DynamicRuleService(DynamicRuleRepository dynamicRuleRepository,
                              RuleStatisticsRepository statisticsRepository,
                              DynamicRuleMapper mapper,
                              DynamicRuleEngine dynamicRuleEngine) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.statisticsRepository = statisticsRepository;
        this.mapper = mapper;
        this.dynamicRuleEngine = dynamicRuleEngine;
    }

    @Transactional
    public DynamicRuleResponse createRule(DynamicRuleRequest request) {
        validateRuleRequest(request);
        log.debug("Creating new dynamic rule for product: {}", request.getProductName());

        DynamicRule rule = mapper.toEntity(request);
        DynamicRule saved = dynamicRuleRepository.save(rule);

        statisticsRepository.createStatisticsEntry(saved.getId());

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

    @Transactional
    public void deleteRule(UUID ruleId) {
        log.debug("Attempting to delete dynamic rule with ID: {}", ruleId);

        if (!dynamicRuleRepository.existsById(ruleId)) {
            throw new DynamicRuleNotFoundException(ruleId);
        }

        statisticsRepository.deleteByRuleId(ruleId);
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
                boolean ruleTriggered = dynamicRuleEngine.evaluateRule(rule, userId, metrics);

                if (ruleTriggered) {
                    // Increment statistics
                    statisticsRepository.incrementTriggerCount(rule.getId());

                    results.add(new RecommendationDto(
                            rule.getProductId(),
                            rule.getProductName(),
                            rule.getProductText()
                    ));
                }
            }

            log.debug("Found {} dynamic recommendations for user: {}", results.size(), userId);
            return results;

        } catch (Exception e) {
            log.error("Error evaluating dynamic rules for user: {}", userId, e);
            return List.of();
        }
    }

    public RuleStatisticsListResponse getRuleStatistics() {
        log.debug("Retrieving rule statistics");

        List<DynamicRule> allRules = dynamicRuleRepository.findAll();
        List<RuleStatisticResponse> stats = allRules.stream()
                .map(rule -> {
                    Long count = statisticsRepository.findByRuleId(rule.getId())
                            .map(RuleStatistics::getTriggerCount)
                            .orElse(0L);
                    return new RuleStatisticResponse(rule.getId(), count);
                })
                .collect(Collectors.toList());

        return new RuleStatisticsListResponse(stats);
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