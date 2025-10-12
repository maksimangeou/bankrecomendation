package pro.sky.bankrecomendation.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:file:/Users/maksimzadoroznyj/IdeaProjects/Recomendation/data/transaction")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build();
    }

    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource(DynamicDataSourceProperties properties) {
        return DataSourceBuilder.create()
                .url(properties.getUrl())
                .driverClassName(properties.getDriverClassName())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
    }

    @Primary
    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate() {
        return new JdbcTemplate(primaryDataSource());
    }

    @Bean(name = "secondaryJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate() {
        return new JdbcTemplate(secondaryDataSource(null));
    }

    @Bean(name = "secondaryJdbcOperations")
    public NamedParameterJdbcTemplate secondaryJdbcOperations() {
        return new NamedParameterJdbcTemplate(secondaryDataSource(null));
    }

    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager secondaryTransactionManager() {
        return new DataSourceTransactionManager(secondaryDataSource(null));
    }
}