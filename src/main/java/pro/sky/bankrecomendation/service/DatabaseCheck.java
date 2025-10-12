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

            // Проверяем существование таблицы
            try {
                secondaryJdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM dynamic_rules", Integer.class);
                log.info("✅ Table 'dynamic_rules' exists");
            } catch (Exception e) {
                log.warn("⚠️ Table 'dynamic_rules' does not exist. Please create it manually:");
                log.warn("""
                    CREATE TABLE dynamic_rules (
                        id UUID PRIMARY KEY,
                        product_name VARCHAR(255) NOT NULL,
                        product_id UUID NOT NULL,
                        product_text TEXT NOT NULL,
                        rule_json TEXT NOT NULL
                    );
                    CREATE INDEX idx_dynamic_rules_product_id ON dynamic_rules(product_id);
                    CREATE INDEX idx_dynamic_rules_product_name ON dynamic_rules(product_name);
                    """);
            }

        } catch (Exception e) {
            log.error("❌ Failed to connect to PostgreSQL: {}", e.getMessage());
        }
    }
}