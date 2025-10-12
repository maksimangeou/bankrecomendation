package pro.sky.bankrecomendation.model.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
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
    @CollectionTable(
            name = "condition_arguments",
            joinColumns = @JoinColumn(name = "condition_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"condition_id", "argument"})
    )
    @Column(name = "argument")
    private List<String> arguments = new ArrayList<>();

    @Column(name = "negate", nullable = false)
    private boolean negate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private DynamicRule rule;
}