package pro.sky.bankrecomendation.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCheck implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCheck.class);

    private final JdbcTemplate secondaryJdbcTemplate;

    public DatabaseCheck(JdbcTemplate secondaryJdbcTemplate) {
        this.secondaryJdbcTemplate = secondaryJdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Проверяем подключение к PostgreSQL
            String result = secondaryJdbcTemplate.queryForObject(
                    "SELECT 'PostgreSQL connection successful'", String.class);
            log.info("✅ {}", result);

            // Проверяем существование таблицы dynamic_rules
            if (!tableExists("dynamic_rules")) {
                log.warn("⚠️ Table 'dynamic_rules' does not exist. Creating it...");
                createDynamicRulesTable();
                log.info("✅ Table 'dynamic_rules' created successfully");
            } else {
                log.info("✅ Table 'dynamic_rules' already exists");
            }

        } catch (Exception e) {
            log.error("❌ Failed to connect to PostgreSQL: {}", e.getMessage());
            throw e;
        }
    }

    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)";
            return Boolean.TRUE.equals(secondaryJdbcTemplate.queryForObject(sql, Boolean.class, tableName));
        } catch (Exception e) {
            log.warn("Error checking if table {} exists: {}", tableName, e.getMessage());
            return false;
        }
    }

    private void createDynamicRulesTable() {
        try {
            // Создаем таблицу с IF NOT EXISTS
            secondaryJdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS dynamic_rules (
                    id UUID PRIMARY KEY,
                    product_name VARCHAR(255) NOT NULL,
                    product_id UUID NOT NULL,
                    product_text TEXT NOT NULL,
                    rule_json TEXT NOT NULL
                )
            """);

            // Создаем индексы с IF NOT EXISTS
            secondaryJdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_dynamic_rules_product_id 
                ON dynamic_rules(product_id)
            """);

            secondaryJdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_dynamic_rules_product_name 
                ON dynamic_rules(product_name)
            """);

            log.info("✅ Table and indexes created successfully");

        } catch (Exception e) {
            log.error("❌ Failed to create dynamic_rules table: {}", e.getMessage());
            throw e;
        }
    }
}