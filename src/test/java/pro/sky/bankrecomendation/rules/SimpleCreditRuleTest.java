package pro.sky.bankrecomendation.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.service.rules.SimpleCreditRule;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimpleCreditRuleTest {

    private SimpleCreditRule rule;

    @BeforeEach
    void setUp() {
        rule = new SimpleCreditRule();
    }

    @Test
    void testRuleFires() {
        UUID userId = UUID.randomUUID();
        UserFinancials metrics = new UserFinancials();
        metrics.setCntCreditProducts(0);        // нет кредитных продуктов
        metrics.setSumDebitDeposits(50_000.0); // есть дебетовые средства

        Optional<RecommendationDto> result = rule.applyRuleSet(userId, metrics);
        assertTrue(result.isPresent(), "Правило должно сработать");
        result.ifPresent(rec -> {
            assertEquals("Простой кредит", rec.getName());
        });
    }

    @Test
    void testRuleDoesNotFire() {
        UUID userId = UUID.randomUUID();
        UserFinancials metrics = new UserFinancials();
        metrics.setCntCreditProducts(1);        // уже есть кредитный продукт
        metrics.setSumDebitDeposits(50_000.0);

        Optional<RecommendationDto> result = rule.applyRuleSet(userId, metrics);
        assertFalse(result.isPresent(), "Правило не должно сработать");
    }
}
