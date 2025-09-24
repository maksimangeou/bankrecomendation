package pro.sky.bankrecomendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    public RecommendationService(RecommendationRepository repository, List<RecommendationRuleSet> ruleSets) {
        this.repository = repository;
        this.ruleSets = ruleSets;
    }

    public RecommendationResponse getRecommendations(UUID userId) {
        MDC.put("userId", userId.toString());
        try {
            log.info("Start computing recommendations for user={}", userId);

            UserFinancials metrics = repository.getUserFinancials(userId);
            log.debug("Aggregated metrics: {}", metrics);

            List<RecommendationDto> results = new ArrayList<>();
            for (RecommendationRuleSet rule : ruleSets) {
                rule.applyRuleSet(userId, metrics).ifPresent(rec -> {
                    log.debug("Rule {} matched -> {}", rule.getClass().getSimpleName(), rec);
                    results.add(rec);
                });
            }

            log.info("Finished computing recommendations for user={} -> {} recommendations", userId, results.size());
            return new RecommendationResponse(userId, results);
        } finally {
            MDC.remove("userId");
        }
    }
}
