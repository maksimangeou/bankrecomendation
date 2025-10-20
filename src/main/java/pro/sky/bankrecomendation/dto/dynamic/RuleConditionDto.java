package pro.sky.bankrecomendation.dto.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.sky.bankrecomendation.model.dynamic.QueryType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleConditionDto {
    private QueryType query;
    private List<String> arguments;
    private boolean negate;
}