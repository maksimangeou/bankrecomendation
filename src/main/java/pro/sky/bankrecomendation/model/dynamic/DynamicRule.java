package pro.sky.bankrecomendation.model.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "dynamic_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_text", nullable = false, columnDefinition = "TEXT")
    private String productText;

    // Храним правило как JSON для упрощения
    @Column(name = "rule_json", nullable = false, columnDefinition = "TEXT")
    private String ruleJson;
}