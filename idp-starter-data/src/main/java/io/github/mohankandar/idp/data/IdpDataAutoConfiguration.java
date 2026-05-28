package io.github.mohankandar.idp.data;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@AutoConfiguration
@EnableConfigurationProperties(IdpDataProperties.class)
public class IdpDataAutoConfiguration {

    /** Provide JdbcTemplate if the app didn’t. DataSource itself is created by Spring Boot. */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
