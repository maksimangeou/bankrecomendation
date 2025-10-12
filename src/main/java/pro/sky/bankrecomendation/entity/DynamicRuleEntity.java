package pro.sky.bankrecomendation.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Entity
@Table(name = "dynamic_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicRuleEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_text")
    private String productText;

    /**
     * JSON-массив правил:
     * [
     *   { "query": "USER_OF", "arguments": ["CREDIT"], "negate": true },
     *   { "query": "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW", "arguments": ["DEBIT", ">"], "negate": false }
     * ]
     */
    @Column(name = "rule", columnDefinition = "TEXT")
    private String rule;
}