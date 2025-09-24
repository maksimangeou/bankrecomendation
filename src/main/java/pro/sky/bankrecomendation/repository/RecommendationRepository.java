package pro.sky.bankrecomendation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pro.sky.bankrecomendation.model.UserFinancials;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Repository
public class RecommendationRepository {

    private static final Logger log = LoggerFactory.getLogger(RecommendationRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Возвращает агрегированные финансовые метрики для пользователя.
     * - Логируется SQL и параметры.
     * - Если нет данных/произошла проблема с отсутствием строки, возвращаются нулевые метрики.
     */
    public UserFinancials getUserFinancials(UUID userId) {
        String sql = """
                SELECT
                   COALESCE(SUM(CASE WHEN p.type = 'DEBIT' AND t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) AS sum_debit_deposits,
                   COALESCE(SUM(CASE WHEN p.type = 'DEBIT' AND (t.type = 'WITHDRAW' OR t.type = 'SPEND' OR t.type = 'PAYMENT') THEN t.amount ELSE 0 END), 0) AS sum_debit_spent,
                   COALESCE(SUM(CASE WHEN p.type = 'SAVING' AND t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) AS sum_saving_deposits,
                   COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'DEBIT' THEN p.id END), 0) AS cnt_debit_products,
                   COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'INVEST' THEN p.id END), 0) AS cnt_invest_products,
                   COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'CREDIT' THEN p.id END), 0) AS cnt_credit_products
                FROM TRANSACTIONS t
                JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
                WHERE t.USER_ID = ?
                """;

        // Убираю лишние переводы строк в логе чтобы было удобно читать
        log.debug("Executing getUserFinancials SQL: {} params=[{}]", sql.replaceAll("\\s+", " "), userId);

        try {
            // безопаснее и удобнее — сначала попробовать получить одну строку
            UserFinancials metrics = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRow(rs), userId.toString());
            log.debug("DB returned UserFinancials for user {} -> {}", userId, metrics);
            return metrics;
        } catch (EmptyResultDataAccessException e) {
            // Теоретически агрегации возвращают строку даже при отсутствии данных,
            // но на всякий случай — если ничего не вернулось -> вернуть нули.
            log.warn("No aggregation row for user {}: returning zeroed UserFinancials", userId);
            return new UserFinancials(0,0,0,0.0,0.0,0.0);
        } catch (Exception e) {
            // Логируем полную проблему и пробрасываем дальше (контроллер/глобальная обработка перехватит)
            log.error("Failed to query user financials for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private UserFinancials mapRow(ResultSet rs) throws SQLException {
        UserFinancials m = new UserFinancials();
        m.setSumDebitDeposits(rs.getDouble("sum_debit_deposits"));
        m.setSumDebitSpent(rs.getDouble("sum_debit_spent"));
        m.setSumSavingDeposits(rs.getDouble("sum_saving_deposits"));
        m.setCntDebitProducts(rs.getLong("cnt_debit_products"));
        m.setCntInvestProducts(rs.getLong("cnt_invest_products"));
        m.setCntCreditProducts(rs.getLong("cnt_credit_products"));
        return m;
    }
}
