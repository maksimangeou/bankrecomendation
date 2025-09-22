package pro.sky.bankrecomendation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pro.sky.bankrecomendation.model.UserFinancials;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


@Repository
public class RecommendationRepository {


    private final JdbcTemplate jdbcTemplate;


    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * Получает агрегированные финансовые метрики по пользователю.
     * Все нужные данные выбираются одним SQL-запросом для производительности.
     */
    public UserFinancials getUserFinancials(UUID userId) {
        String sql = "SELECT\n" +
                     " COALESCE(SUM(CASE WHEN p.type = 'DEBIT' AND t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) AS sum_debit_deposits,\n" +
                     " COALESCE(SUM(CASE WHEN p.type = 'DEBIT' AND (t.type = 'WITHDRAW' OR t.type = 'SPEND' OR t.type = 'PAYMENT') THEN t.amount ELSE 0 END), 0) AS sum_debit_spent,\n" +
                     " COALESCE(SUM(CASE WHEN p.type = 'SAVING' AND t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) AS sum_saving_deposits,\n" +
                     " COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'DEBIT' THEN p.id END), 0) AS cnt_debit_products,\n" +
                     " COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'INVEST' THEN p.id END), 0) AS cnt_invest_products,\n" +
                     " COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'CREDIT' THEN p.id END), 0) AS cnt_credit_products\n" +
                     "FROM TRANSACTIONS t\n" +
                     "JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID\n" +
                     "WHERE t.USER_ID = ?\n";


        return jdbcTemplate.queryForObject(sql, new Object[]{userId.toString()}, (rs, rowNum) -> mapRow(rs));
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
