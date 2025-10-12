package pro.sky.bankrecomendation.dto.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRuleRequest {
    private String productName;
    private UUID productId;
    private String productText;
    private List<RuleConditionDto> rule;
}