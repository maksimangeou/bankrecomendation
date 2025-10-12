package pro.sky.bankrecomendation.rules;

import org.junit.jupiter.api.Test;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TopSavingRuleTest {

    private final TopSavingRule rule = new TopSavingRule();

    @Test
    void testRuleFires() {
        UUID userId = UUID.randomUUID();

        // Создаём метрики так, чтобы правило сработало
        UserFinancials metrics = new UserFinancials();
        metrics.setSumSavingDeposits(0);      // нет сбережений
        metrics.setSumDebitDeposits(100_000); // достаточно на дебетовых счетах

        Optional<RecommendationDto> result = rule.applyRuleSet(userId, metrics);

        assertTrue(result.isPresent());
        assertEquals("Накопительный счёт", result.get().getName());
    }

    @Test
    void testRuleDoesNotFire() {
        UUID userId = UUID.randomUUID();

        // Создаём метрики так, чтобы правило не сработало
        UserFinancials metrics = new UserFinancials();
        metrics.setSumSavingDeposits(10_000); // есть сбережения
        metrics.setSumDebitDeposits(100_000);

        Optional<RecommendationDto> result = rule.applyRuleSet(userId, metrics);

        assertFalse(result.isPresent());
    }
}
