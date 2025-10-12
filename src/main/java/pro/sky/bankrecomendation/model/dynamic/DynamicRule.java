package pro.sky.bankrecomendation.model.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
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

    @Column(name = "product_text", nullable = false, length = 1000)
    private String productText;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RuleCondition> conditions;
}