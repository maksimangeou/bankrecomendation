package pro.sky.bankrecomendation.model.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("rule_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleStatistics {
    private UUID ruleId;
    private Long triggerCount;
}