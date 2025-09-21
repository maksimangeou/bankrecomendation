package pro.sky.bankrecomendation.service.rules;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.Recommendation;
import pro.sky.bankrecomendation.service.RecommendationRuleSet;

import java.util.Optional;
import java.util.UUID;

@Component
public class RuleSet1 implements RecommendationRuleSet {

    private final JdbcTemplate jdbcTemplate;

    public RuleSet1(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Recommendation> check(UUID userId) {
        String sql = """
            SELECT p.id, p.name, p.description as text
            FROM products p
            WHERE p.name = 'Invest 500'
              AND EXISTS ( -- Правило 1: Пользователь использует как минимум один продукт типа 'DEBIT'
                  SELECT 1 FROM transactions t
                  JOIN products p_debit ON t.product_id = p_debit.id
                  WHERE t.user_id = ? AND p_debit.type = 'DEBIT'
              )
              AND NOT EXISTS ( -- Правило 2: Пользователь НЕ использует продукты типа 'INVEST'
                  SELECT 1 FROM transactions t
                  JOIN products p_invest ON t.product_id = p_invest.id
                  WHERE t.user_id = ? AND p_invest.type = 'INVEST'
              )
              AND ( -- Правило 3: Сумма пополнений по продуктам типа 'SAVING' > 1000
                  SELECT COALESCE(SUM(t.amount), 0)
                  FROM transactions t
                  JOIN products p_saving ON t.product_id = p_saving.id
                  WHERE t.user_id = ? AND p_saving.type = 'SAVING' AND t.type = 'DEPOSIT'
              ) > 1000
            """;

        try {
            Recommendation rec = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> new Recommendation(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("name"),
                            rs.getString("text")
                    ),
                    userId, userId, userId // Подставляем userId для трех параметров (?) в запросе
            );
            return Optional.ofNullable(rec);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}