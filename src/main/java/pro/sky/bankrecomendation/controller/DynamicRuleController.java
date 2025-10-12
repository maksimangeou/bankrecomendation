package pro.sky.bankrecomendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.sky.bankrecomendation.dto.dynamic.*;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;
import pro.sky.bankrecomendation.model.dynamic.RuleCondition;
import pro.sky.bankrecomendation.repository.dynamic.DynamicRuleRepository;
import pro.sky.bankrecomendation.service.DynamicRuleMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rule")
public class DynamicRuleController {

    private final DynamicRuleRepository dynamicRuleRepository;
    private final DynamicRuleMapper mapper;

    public DynamicRuleController(DynamicRuleRepository dynamicRuleRepository,
                                 DynamicRuleMapper mapper) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<DynamicRuleResponse> createRule(@RequestBody DynamicRuleRequest request) {
        DynamicRule rule = mapper.toEntity(request);
        DynamicRule saved = dynamicRuleRepository.save(rule);
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<DynamicRulesListResponse> getAllRules() {
        List<DynamicRule> rules = dynamicRuleRepository.findAll();
        List<DynamicRuleResponse> responseList = rules.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new DynamicRulesListResponse(responseList));
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId) {
        if (dynamicRuleRepository.existsById(ruleId)) {
            dynamicRuleRepository.deleteById(ruleId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}