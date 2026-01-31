package com.jobportal.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Production-grade Database Configuration
 * 
 * Features:
 * - Optimized HikariCP connection pool
 * - Environment-specific configurations
 * - Connection health monitoring
 * - Performance optimization
 * - Transaction management
 * - Database metrics collection
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minIdleConnections;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1200000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Bean
    @Primary
    @Profile("!test")
    public DataSource productionDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic configuration
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // Pool configuration
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdleConnections);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);

        // Performance optimizations
        config.setAutoCommit(false); // Better transaction control
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        // Connection pool name for monitoring
        config.setPoolName("JobPortal-DB-Pool");

        // Additional PostgreSQL optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // Health check configuration
        config.setHealthCheckRegistry(null); // Will be set up by monitoring
        
        log.info("Configuring HikariCP with maxPoolSize: {}, minIdle: {}, connectionTimeout: {}ms", 
                maxPoolSize, minIdleConnections, connectionTimeout);

        return new HikariDataSource(config);
    }

    /**
     * Test profile data source with minimal configuration
     */
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setPoolName("JobPortal-Test-Pool");
        
        return new HikariDataSource(config);
    }

    /**
     * Transaction manager with optimizations
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        
        // Transaction optimizations
        transactionManager.setDefaultTimeout(30); // 30 seconds timeout
        transactionManager.setRollbackOnCommitFailure(true);
        transactionManager.setValidateExistingTransaction(true);
        
        log.info("Configured JPA Transaction Manager with 30s timeout");
        
        return transactionManager;
    }

    /**
     * Database health checker
     */
    @Bean
    public DatabaseHealthChecker databaseHealthChecker() {
        return new DatabaseHealthChecker();
    }

    /**
     * Database health monitoring component
     */
    public static class DatabaseHealthChecker {
        
        public boolean isHealthy(DataSource dataSource) {
            try {
                return dataSource.getConnection().isValid(5);
            } catch (Exception e) {
                log.error("Database health check failed", e);
                return false;
            }
        }
        
        public DatabaseStats getStats(DataSource dataSource) {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                return new DatabaseStats(
                    hikariDS.getHikariPoolMXBean().getActiveConnections(),
                    hikariDS.getHikariPoolMXBean().getIdleConnections(),
                    hikariDS.getHikariPoolMXBean().getTotalConnections(),
                    hikariDS.getHikariPoolMXBean().getThreadsAwaitingConnection()
                );
            }
            return new DatabaseStats(0, 0, 0, 0);
        }
    }

    /**
     * Database statistics record
     */
    public record DatabaseStats(
        int activeConnections,
        int idleConnections,
        int totalConnections,
        int threadsAwaitingConnection
    ) {}
}
