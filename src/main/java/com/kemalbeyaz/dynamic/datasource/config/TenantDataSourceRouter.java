package com.kemalbeyaz.dynamic.datasource.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TenantDataSourceRouter extends AbstractRoutingDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantDataSourceRouter.class);

    private static final String COUNT_TABLE_BY_TENANT_QUERY = "select count(*) as table_count from information_schema.TABLES where TABLE_SCHEMA = ?";
    private static final String TABLE_COUNT_COLUMN = "table_count";

    @Value("${tenant.default.username}")
    private String tenantsDefaultUsername;

    @Value("${tenant.default.password}")
    private String tenantsDefaultPassword;

    private final HikariDataSource defaultDataSource;
    private final String driverClassName;
    private final String urlTemplate;

    private final Map<String, HikariDataSource> resolvedDataSources = new ConcurrentHashMap<>();

    public TenantDataSourceRouter(HikariDataSource defaultDataSource,
                                  String driverClassName, String urlTemplate) {
        this.defaultDataSource = defaultDataSource;
        this.driverClassName = driverClassName;
        this.urlTemplate = urlTemplate;

        // required by AbstractRoutingDataSource
        setTargetDataSources(new HashMap<>());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContextHolder.getTenantKey();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String tenantId = (String) determineCurrentLookupKey();
        return determineDataSource(tenantId);
    }

    private synchronized DataSource determineDataSource(String tenantId) {
        try {
            if (tenantId == null || tenantId.equals(ConfigurationConstants.DEFAULT_TENANT_KEY)) {
                return defaultDataSource;
            }

            if (!resolvedDataSources.containsKey(tenantId)) {
                var dataSource = createDataSourceIfTenantExist(tenantId);
                resolvedDataSources.put(tenantId, dataSource);
                return dataSource;
            }

            return resolvedDataSources.get(tenantId);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot determine DataSource for tenant: " + tenantId, ex);
        }
    }


    private HikariDataSource createDataSourceIfTenantExist(String tenantId) {
        try {
            return createAndInitIfRequiredDataSourceForTenant(tenantId);
        } catch (TenantInitializationException tex) {
            LOGGER.info("Tenant initialize has failed: {}", tenantId);
            throw new IllegalStateException("Tenant initialize has failed: " + tenantId, tex);
        }
    }

    private HikariDataSource createAndInitIfRequiredDataSourceForTenant(String tenant) throws TenantInitializationException {
        String jdbcUrl = String.format(urlTemplate, tenant);
        var hikariConfig = createDefaultHikariConfig(driverClassName, jdbcUrl,
                tenantsDefaultUsername, tenantsDefaultPassword);
        hikariConfig.setPoolName("HikariPool-" + tenant);
        var dataSource = new HikariDataSource(hikariConfig);

        try {
            initializeDataSourceIfRequired(tenant, dataSource);
        } catch (SQLException ex) {
            throw new TenantInitializationException("Error during initialization tables on db", ex);
        }

        return dataSource;
    }

    private void initializeDataSourceIfRequired(String tenant, DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(COUNT_TABLE_BY_TENANT_QUERY)
        ) {
            statement.setString(1, tenant);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getLong(TABLE_COUNT_COLUMN) == 0) {
                    LOGGER.info("Schema initialize started for tenant {}", tenant);
                    initializeSchema(dataSource);
                    LOGGER.info("Schema initialized for tenant {}", tenant);
                }
            }
        }
    }

    private void initializeSchema(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("create table PERSON (\n" +
                        "                        id bigint not null auto_increment,\n" +
                        "                        name varchar(255),\n" +
                        "                        primary key (id)\n" +
                        ")");
            }
        }
    }

    public HikariConfig createDefaultHikariConfig(String driverClassNamex, String jdbcUrl,
                                                  String username, String password) {
        var hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassNamex);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.addDataSourceProperty("useCompression", true);
        hikariConfig.addDataSourceProperty("autoReconnect", true);
        hikariConfig.addDataSourceProperty("validationQuery", "SELECT 1");
        hikariConfig.addDataSourceProperty("testOnBorrow", true);
        return hikariConfig;
    }

    private static class TenantInitializationException extends Exception {
        TenantInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
