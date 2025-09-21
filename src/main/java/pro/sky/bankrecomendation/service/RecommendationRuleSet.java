package pro.sky.bankrecomendation.service;

import pro.sky.bankrecomendation.dto.Recommendation;

import java.util.Optional;
import java.util.UUID;

public interface RecommendationRuleSet {

    Optional<Recommendation> check(UUID userId);
}
