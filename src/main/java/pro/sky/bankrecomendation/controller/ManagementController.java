package pro.sky.bankrecomendation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.bankrecomendation.repository.RecommendationRepository;

@RestController
@RequestMapping("/management")
public class ManagementController {

    private static final Logger log = LoggerFactory.getLogger(ManagementController.class);
    private final RecommendationRepository recommendationRepository;

    public ManagementController(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        log.info("Clearing all caches");
        recommendationRepository.clearCache();
        return ResponseEntity.ok().build();
    }
}