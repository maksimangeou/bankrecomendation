package pro.sky.bankrecomendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.dto.RecommendationResponse;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.repository.RecommendationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RecommendationRepository repository;
    private final List<RecommendationRuleSet> ruleSets;
    private final DynamicRuleService dynamicRuleService;

    public RecommendationService(RecommendationRepository repository,
                                 List<RecommendationRuleSet> ruleSets,
                                 DynamicRuleService dynamicRuleService) {
        this.repository = repository;
        this.ruleSets = ruleSets;
        this.dynamicRuleService = dynamicRuleService;
    }

    public RecommendationResponse getRecommendations(UUID userId) {
        log.debug("Start computing recommendations for user={} ", userId);

        UserFinancials metrics = repository.getUserFinancials(userId);

        List<RecommendationDto> results = new ArrayList<>();

        for (RecommendationRuleSet r : ruleSets) {
            r.applyRuleSet(userId, metrics).ifPresent(results::add);
        }

        List<RecommendationDto> dynamicResults = dynamicRuleService.evaluateDynamicRules(userId, metrics);
        results.addAll(dynamicResults);

        return new RecommendationResponse(userId, results);
    }
}