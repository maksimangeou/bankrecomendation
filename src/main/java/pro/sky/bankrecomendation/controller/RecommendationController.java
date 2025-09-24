package pro.sky.bankrecomendation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.sky.bankrecomendation.dto.RecommendationResponse;
import pro.sky.bankrecomendation.service.RecommendationService;

import java.util.UUID;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationResponse> getRecommendation(@PathVariable UUID userId) {
        log.info("Received recommendation request for user={}", userId);
        RecommendationResponse resp = recommendationService.getRecommendations(userId);
        log.info("Returning {} recommendations for user={}", resp.getRecommendations().size(), userId);
        return ResponseEntity.ok(resp);
    }
}
