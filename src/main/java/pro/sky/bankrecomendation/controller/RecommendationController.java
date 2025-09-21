package pro.sky.bankrecomendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.service.RecommendationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getRecommendations(@PathVariable UUID userId) {
        List<RecommendationDto> recommendations = recommendationService.getRecommendationsForUser(userId);

        // Формируем ответ строго по ТЗ
        Map<String, Object> response = new HashMap<>();
        response.put("user_id", userId.toString());
        response.put("recommendations", recommendations);

        return ResponseEntity.ok(response);
    }
}