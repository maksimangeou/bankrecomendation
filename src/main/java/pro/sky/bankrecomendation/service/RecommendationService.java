package pro.sky.bankrecomendation.service;

import org.springframework.stereotype.Service;
import pro.sky.bankrecomendation.dto.Recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;

    public RecommendationService(List<RecommendationRuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public List<Recommendation> getRecommendationsForUser(UUID userId) {
        List<Recommendation> result = new ArrayList<>();
        for (RecommendationRuleSet ruleSet : ruleSets) {
            ruleSet.check(userId).ifPresent(result::add);
        }
        return result;
    }
}