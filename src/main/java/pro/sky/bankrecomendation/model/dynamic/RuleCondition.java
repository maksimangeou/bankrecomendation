package pro.sky.bankrecomendation.model.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rule_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", nullable = false)
    private QueryType query;

    @ElementCollection
    @CollectionTable(name = "condition_arguments", joinColumns = @JoinColumn(name = "condition_id"))
    @Column(name = "argument")
    private List<String> arguments;

    @Column(name = "negate", nullable = false)
    private boolean negate;

    @ManyToOne
    @JoinColumn(name = "rule_id")
    private DynamicRule rule;
}