package pro.sky.bankrecomendation.service;

import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleRequest;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleResponse;
import pro.sky.bankrecomendation.dto.dynamic.RuleConditionDto;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.model.dynamic.RuleCondition;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DynamicRuleMapper {

    public DynamicRule toEntity(DynamicRuleRequest request) {
        DynamicRule rule = new DynamicRule();
        rule.setId(UUID.randomUUID());
        rule.setProductName(request.getProductName());
        rule.setProductId(request.getProductId());
        rule.setProductText(request.getProductText());

        List<RuleCondition> conditions = request.getRule().stream()
                .map(this::toConditionEntity)
                .peek(condition -> condition.setRule(rule))
                .collect(Collectors.toList());

        rule.setConditions(conditions);
        return rule;
    }

    public DynamicRuleResponse toResponse(DynamicRule entity) {
        List<RuleConditionDto> conditionDtos = entity.getConditions().stream()
                .map(this::toConditionDto)
                .collect(Collectors.toList());

        return new DynamicRuleResponse(
                entity.getId(),
                entity.getProductName(),
                entity.getProductId(),
                entity.getProductText(),
                conditionDtos
        );
    }

    private RuleCondition toConditionEntity(RuleConditionDto dto) {
        RuleCondition condition = new RuleCondition();
        condition.setQuery(dto.getQuery());
        condition.setArguments(dto.getArguments());
        condition.setNegate(dto.isNegate());
        return condition;
    }

    private RuleConditionDto toConditionDto(RuleCondition entity) {
        return new RuleConditionDto(
                entity.getQuery(),
                entity.getArguments(),
                entity.isNegate()
        );
    }
}