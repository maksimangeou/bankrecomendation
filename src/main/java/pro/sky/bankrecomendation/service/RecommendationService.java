package pro.sky.bankrecomendation.service;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import pro.sky.bankrecomendation.dto.RecommendationDto;

import org.slf4j.LoggerFactory;
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


    /**
     * Получаем рекомендации для пользователя: агрегируем данные одним запросом и применяем правила.
     */
    public RecommendationResponse getRecommendations(UUID userId) {
        log.debug("Start computing recommendations for user={} ", userId);


// Получаем агрегированные финансовые метрики одним запросом
        UserFinancials metrics = repository.getUserFinancials(userId);


        List<RecommendationDto> results = new ArrayList<>();
        for (RecommendationRuleSet r : ruleSets) {
            r.apply(userId, metrics).ifPresent(results::add);
        }


// Формируем ответ строго в нужном формате
        return new RecommendationResponse(userId, results);
    }
}