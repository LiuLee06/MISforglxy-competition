package com.sdjzuxg.collegemanagesystem.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 主数据源（原有业务库）的显式声明。
 *
 * 背景：成果管理模块需要连接独立的本地成果库（见 AchievementDataSourceConfig），
 * 一旦容器中出现两个 DataSource，Spring Boot 的数据源/MyBatis 自动配置会因
 * "无法判定唯一候选" 而失效，导致原有 Mapper 全部失效。
 *
 * 因此这里把主数据源与主 SqlSessionFactory 显式注册为 @Primary，
 * 保证：①原有 spring.datasource 配置与 mybatis.configuration 行为完全不变；
 *      ②原有 Mapper 依旧绑定主数据源，功能不受任何影响。
 */
@Configuration
@MapperScan(basePackages = {"com.sdjzuxg.collegemanagesystem.mapper", "com.sdjzuxg.collegemanagesystem.agent.mapper", "com.sdjzuxg.collegemanagesystem.knowledge.mapper"},
        sqlSessionFactoryRef = "primarySqlSessionFactory")
public class PrimaryMybatisConfig {

    /**
     * 主数据源：复用 Spring Boot 的 DataSourceProperties（读取 spring.datasource.*），
     * 连接池参数再按 spring.datasource.hikari.* 绑定，
     * 与 Boot 自带的数据源创建方式完全等价，不改变原有配置含义。
     */
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "primarySqlSessionFactory")
    @Primary
    public SqlSessionFactory primarySqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(resolve("classpath*:mapper/*Mapper.xml"));
        // 与 application.yaml 中 mybatis.configuration.map-underscore-to-camel-case 保持一致
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    @Bean(name = "transactionManager")
    @Primary
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private Resource[] resolve(String pattern) throws Exception {
        return new PathMatchingResourcePatternResolver().getResources(pattern);
    }
}
