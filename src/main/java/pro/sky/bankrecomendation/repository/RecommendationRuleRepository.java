package pro.sky.bankrecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.bankrecomendation.entity.RecommendationRule;


public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, Long> {
}
