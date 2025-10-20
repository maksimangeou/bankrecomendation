package pro.sky.bankrecomendation.repository.dynamic;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DynamicRuleRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<DynamicRule> rowMapper;

    public DynamicRuleRepository(JdbcTemplate secondaryJdbcTemplate) {
        this.jdbcTemplate = secondaryJdbcTemplate;
        this.rowMapper = (rs, rowNum) -> new DynamicRule(
                UUID.fromString(rs.getString("id")),
                rs.getString("product_name"),
                UUID.fromString(rs.getString("product_id")),
                rs.getString("product_text"),
                rs.getString("rule_json")
        );
    }

    public List<DynamicRule> findAll() {
        String sql = "SELECT * FROM dynamic_rules";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<DynamicRule> findById(UUID id) {
        String sql = "SELECT * FROM dynamic_rules WHERE id = ?";
        List<DynamicRule> rules = jdbcTemplate.query(sql, rowMapper, id.toString());
        return rules.isEmpty() ? Optional.empty() : Optional.of(rules.get(0));
    }

    public DynamicRule save(DynamicRule rule) {
        if (rule.getId() == null) {
            rule.setId(UUID.randomUUID());
        }

        String sql = "INSERT INTO dynamic_rules (id, product_name, product_id, product_text, rule_json) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                rule.getId().toString(),
                rule.getProductName(),
                rule.getProductId().toString(),
                rule.getProductText(),
                rule.getRuleJson());

        return rule;
    }

    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM dynamic_rules WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.toString());
        return count != null && count > 0;
    }

    public boolean existsByProductId(UUID productId) {
        String sql = "SELECT COUNT(*) FROM dynamic_rules WHERE product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId.toString());
        return count != null && count > 0;
    }

    public void deleteById(UUID id) {
        String sql = "DELETE FROM dynamic_rules WHERE id = ?";
        jdbcTemplate.update(sql, id.toString());
    }

    public void deleteByProductId(UUID productId) {
        String sql = "DELETE FROM dynamic_rules WHERE product_id = ?";
        jdbcTemplate.update(sql, productId.toString());
    }
}