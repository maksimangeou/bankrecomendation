package pro.sky.bankrecomendation.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "recommendation_rules")
@Data
public class RecommendationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;       // Уникальный код правила
    private String title;      // Название
    private String condition;  // SQL/JSON/Script
    private String message;    // Текст рекомендации
    private Boolean active;
}
