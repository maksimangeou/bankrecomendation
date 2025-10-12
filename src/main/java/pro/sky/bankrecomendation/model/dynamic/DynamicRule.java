package pro.sky.bankrecomendation.model.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("dynamic_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRule {

    @Id
    private UUID id;
    private String productName;
    private UUID productId;
    private String productText;
    private String ruleJson;
}