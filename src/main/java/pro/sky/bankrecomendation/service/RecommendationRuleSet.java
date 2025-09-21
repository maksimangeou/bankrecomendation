package pro.sky.bankrecomendation.service;

import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;

import java.util.Optional;
import java.util.UUID;

public interface RecommendationRuleSet {
    /**
     * Проверяет правила для пользователя, используя агрегированные метрики.
     * Возвращает Optional с RecommendationDto если правило выполнено.
     */
    Optional<RecommendationDto> apply(UUID userId, UserFinancials metrics);
}
