package pro.sky.bankrecomendation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleRequest;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRuleResponse;
import pro.sky.bankrecomendation.dto.dynamic.DynamicRulesListResponse;
import pro.sky.bankrecomendation.service.DynamicRuleService;

import java.util.UUID;

@RestController
@RequestMapping("/rule")
public class DynamicRuleController {

    private static final Logger log = LoggerFactory.getLogger(DynamicRuleController.class);

    private final DynamicRuleService dynamicRuleService;

    public DynamicRuleController(DynamicRuleService dynamicRuleService) {
        this.dynamicRuleService = dynamicRuleService;
    }

    @PostMapping
    public ResponseEntity<DynamicRuleResponse> createRule(@RequestBody DynamicRuleRequest request) {
        log.debug("POST /rule - Creating new dynamic rule");
        DynamicRuleResponse response = dynamicRuleService.createRule(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<DynamicRulesListResponse> getAllRules() {
        log.debug("GET /rule - Retrieving all dynamic rules");
        DynamicRulesListResponse response = dynamicRuleService.getAllRules();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<DynamicRuleResponse> getRule(@PathVariable UUID ruleId) {
        log.debug("GET /rule/{} - Retrieving dynamic rule", ruleId);
        DynamicRuleResponse response = dynamicRuleService.getRuleById(ruleId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId) {
        log.debug("DELETE /rule/{} - Deleting dynamic rule", ruleId);
        dynamicRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}