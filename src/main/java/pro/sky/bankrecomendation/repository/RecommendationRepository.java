package pro.sky.bankrecomendation.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pro.sky.bankrecomendation.model.UserFinancials;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class RecommendationRepository {

    private final JdbcTemplate jdbcTemplate;

    // Кеш для агрегированных финансовых метрик пользователя
    private final Cache<UUID, UserFinancials> userFinancialsCache;

    // Кеш для проверок использования продуктов
    private final Cache<String, Boolean> productUsageCache;

    // Кеш для сумм транзакций
    private final Cache<String, Double> transactionSumCache;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        this.userFinancialsCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.productUsageCache = Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.transactionSumCache = Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public UserFinancials getUserFinancials(UUID userId) {
        return userFinancialsCache.get(userId, key -> {
            String sql = "SELECT\n" +
                         " COALESCE(SUM(CASE WHEN p.type = 'DEBIT' AND t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) AS sum_debit_deposits,\n" +
                         " COALESCE(SUM(CASE WHEN p.type = 'DEBIT' AND (t.type = 'WITHDRAW' OR t.type = 'SPEND' OR t.type = 'PAYMENT') THEN t.amount ELSE 0 END), 0) AS sum_debit_spent,\n" +
                         " COALESCE(SUM(CASE WHEN p.type = 'SAVING' AND t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) AS sum_saving_deposits,\n" +
                         " COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'DEBIT' THEN p.id END), 0) AS cnt_debit_products,\n" +
                         " COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'INVEST' THEN p.id END), 0) AS cnt_invest_products,\n" +
                         " COALESCE(COUNT(DISTINCT CASE WHEN p.type = 'CREDIT' THEN p.id END), 0) AS cnt_credit_products\n" +
                         "FROM TRANSACTIONS t\n" +
                         "JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID\n" +
                         "WHERE t.USER_ID = ?";

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRow(rs), userId.toString());
        });
    }

    public boolean isUserOfProductType(UUID userId, String productType) {
        String cacheKey = userId + ":" + productType + ":user_of";
        return productUsageCache.get(cacheKey, key -> {
            String sql = "SELECT COUNT(*) > 0 FROM TRANSACTIONS t " +
                         "JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID " +
                         "WHERE t.USER_ID = ? AND p.type = ?";
            return jdbcTemplate.queryForObject(sql, Boolean.class, userId.toString(), productType);
        });
    }

    public boolean isActiveUserOfProductType(UUID userId, String productType) {
        String cacheKey = userId + ":" + productType + ":active_user";
        return productUsageCache.get(cacheKey, key -> {
            String sql = "SELECT COUNT(*) >= 5 FROM TRANSACTIONS t " +
                         "JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID " +
                         "WHERE t.USER_ID = ? AND p.type = ?";
            return jdbcTemplate.queryForObject(sql, Boolean.class, userId.toString(), productType);
        });
    }

    public Double getTransactionSum(UUID userId, String productType, String transactionType) {
        String cacheKey = userId + ":" + productType + ":" + transactionType;
        return transactionSumCache.get(cacheKey, key -> {
            String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM TRANSACTIONS t " +
                         "JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID " +
                         "WHERE t.USER_ID = ? AND p.type = ? AND t.type = ?";
            return jdbcTemplate.queryForObject(sql, Double.class, userId.toString(), productType, transactionType);
        });
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

    // Метод для очистки кеша (может быть полезен при тестировании)
    public void clearCache() {
        userFinancialsCache.invalidateAll();
        productUsageCache.invalidateAll();
        transactionSumCache.invalidateAll();
    }
}