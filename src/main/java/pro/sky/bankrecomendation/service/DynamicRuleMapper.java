package pro.sky.bankrecomendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleRequest;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleResponse;
import pro.sky.bankrecomendation.dto.dynamic.RuleConditionDto;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;

import java.util.List;
import java.util.UUID;

@Component
public class DynamicRuleMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DynamicRule toEntity(DynamicRuleRequest request) {
        DynamicRule rule = new DynamicRule();
        rule.setId(UUID.randomUUID());
        rule.setProductName(request.getProductName());
        rule.setProductId(request.getProductId());
        rule.setProductText(request.getProductText());

        try {
            rule.setRuleJson(objectMapper.writeValueAsString(request.getRule()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing rule to JSON", e);
        }

        return rule;
    }

    public DynamicRuleResponse toResponse(DynamicRule entity) {
        try {
            List<RuleConditionDto> rule = objectMapper.readValue(
                    entity.getRuleJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RuleConditionDto.class)
            );

            return new DynamicRuleResponse(
                    entity.getId(),
                    entity.getProductName(),
                    entity.getProductId(),
                    entity.getProductText(),
                    rule
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing rule from JSON", e);
        }
    }
}