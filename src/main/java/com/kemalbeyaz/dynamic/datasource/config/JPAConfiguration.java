package com.kemalbeyaz.dynamic.datasource.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class JPAConfiguration {

    // defaults
    @Value("${default.datasource.driverClassName}")
    private String driverClassName;
    @Value("${default.datasource.jdbcUrl}")
    private String jdbcUrl;
    @Value("${default.datasource.username}")
    private String username;
    @Value("${default.datasource.password}")
    private String password;

    // for tenant
    @Value("${tenant.driverClassName}")
    private String driverClassNameForTenant;

    @Value("${tenant.url-template}")
    private String urlTemplate;

    @Primary
    @Bean
    public DataSource routingDatasource() {
        return new TenantDataSourceRouter(
                createDefaultTenantDataSource(),
                driverClassNameForTenant,
                urlTemplate
        );
    }

    public HikariConfig createDefaultHikariConfig() {
        var hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        return hikariConfig;
    }

    private HikariDataSource createDefaultTenantDataSource() {
        var config = createDefaultHikariConfig();

        config.addDataSourceProperty("useCompression", true);
        config.addDataSourceProperty("autoReconnect", true);
        config.addDataSourceProperty("validationQuery", "SELECT 1");
        config.addDataSourceProperty("testOnBorrow", true);
        return new HikariDataSource(config);
    }
}
