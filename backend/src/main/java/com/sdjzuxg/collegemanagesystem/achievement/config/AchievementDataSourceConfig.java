package com.sdjzuxg.collegemanagesystem.achievement.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 成果管理模块专用数据源：连接成果本地库（默认 achievement_system）。
 *
 * 连接方式通过 application.yaml 中新增的 achievement.datasource.* 节点配置，
 * 与原有 spring.datasource 完全隔离：原库任何表都不读写，原 Mapper 仍走主数据源
 * （主数据源已在 PrimaryMybatisConfig 中显式注册为 @Primary）。
 */
@Configuration
@MapperScan(basePackages = "com.sdjzuxg.collegemanagesystem.achievement.mapper",
        sqlSessionFactoryRef = "achievementSqlSessionFactory")
public class AchievementDataSourceConfig {

    /**
     * 成果专用数据源：属性命名与 spring.datasource 保持一致的风格
     * （url / username / password / driver-class-name），连接池参数再按
     * achievement.datasource.hikari.* 绑定，仅是新增节点，不影响原有配置。
     *
     * 说明：这里没有定义第二个 DataSourceProperties bean，避免与 Boot 自带的
     * spring.datasource 属性源产生类型冲突，导致主数据源注入歧义。
     */
    @Bean(name = "achievementDataSource")
    @ConfigurationProperties(prefix = "achievement.datasource.hikari")
    public DataSource achievementDataSource(
            @Value("${achievement.datasource.url}") String url,
            @Value("${achievement.datasource.username}") String username,
            @Value("${achievement.datasource.password}") String password,
            @Value("${achievement.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}") String driverClassName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean(name = "achievementSqlSessionFactory")
    public SqlSessionFactory achievementSqlSessionFactory(@Qualifier("achievementDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(resolve("classpath*:mapper/achievement/*Mapper.xml"));
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    @Bean(name = "achievementTransactionManager")
    public DataSourceTransactionManager achievementTransactionManager(@Qualifier("achievementDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private Resource[] resolve(String pattern) throws Exception {
        return new PathMatchingResourcePatternResolver().getResources(pattern);
    }
}
