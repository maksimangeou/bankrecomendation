package pro.sky.bankrecomendation.repository.dynamic;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.sky.bankrecomendation.model.dynamic.RuleStatistics;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RuleStatisticsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<RuleStatistics> rowMapper;

    public RuleStatisticsRepository(JdbcTemplate secondaryJdbcTemplate) {
        this.jdbcTemplate = secondaryJdbcTemplate;
        this.rowMapper = (rs, rowNum) -> new RuleStatistics(
                UUID.fromString(rs.getString("rule_id")),
                rs.getLong("trigger_count")
        );
    }

    public List<RuleStatistics> findAll() {
        String sql = "SELECT rs.* FROM rule_statistics rs " +
                     "JOIN dynamic_rules dr ON rs.rule_id = dr.id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<RuleStatistics> findByRuleId(UUID ruleId) {
        String sql = "SELECT * FROM rule_statistics WHERE rule_id = ?";
        List<RuleStatistics> stats = jdbcTemplate.query(sql, rowMapper, ruleId.toString());
        return stats.isEmpty() ? Optional.empty() : Optional.of(stats.get(0));
    }

    public void incrementTriggerCount(UUID ruleId) {
        String sql = "INSERT INTO rule_statistics (rule_id, trigger_count) VALUES (?, 1) " +
                     "ON CONFLICT (rule_id) DO UPDATE SET trigger_count = rule_statistics.trigger_count + 1";
        jdbcTemplate.update(sql, ruleId.toString());
    }

    public void deleteByRuleId(UUID ruleId) {
        String sql = "DELETE FROM rule_statistics WHERE rule_id = ?";
        jdbcTemplate.update(sql, ruleId.toString());
    }

    public void createStatisticsEntry(UUID ruleId) {
        String sql = "INSERT INTO rule_statistics (rule_id, trigger_count) VALUES (?, 0) " +
                     "ON CONFLICT (rule_id) DO NOTHING";
        jdbcTemplate.update(sql, ruleId.toString());
    }
}