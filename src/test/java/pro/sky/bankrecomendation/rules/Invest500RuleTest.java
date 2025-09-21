package pro.sky.bankrecomendation.rules;

import org.junit.jupiter.api.Test;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.service.rules.Invest500Rule;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Invest500RuleTest {


    @Test
    void whenConditionsMet_thenRecommend() {
        Invest500Rule rule = new Invest500Rule();
        UserFinancials m = new UserFinancials(1, 0, 0, 100.0, 10.0, 2000.0);
        Optional<?> r = rule.apply(UUID.randomUUID(), m);
        assertTrue(r.isPresent());
    }


    @Test
    void whenNoSavings_thenNotRecommend() {
        Invest500Rule rule = new Invest500Rule();
        UserFinancials m = new UserFinancials(1, 0, 0, 100.0, 10.0, 500.0);
        Optional<?> r = rule.apply(UUID.randomUUID(), m);
        assertFalse(r.isPresent());
    }
}
