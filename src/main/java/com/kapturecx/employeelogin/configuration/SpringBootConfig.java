package com.kapturecx.employeelogin.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapturecx.employeelogin.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class SpringBootConfig {


    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;

    @Value("${hibernate.format_sql}")
    private String hibernateFormatSql;

    @Value("${hibernate.hbm2ddl.auto}")
    private String hibernateHbm2ddlAuto;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(datasourceUrl);
        dataSource.setUsername(datasourceUsername);
        dataSource.setPassword(datasourcePassword);
        return dataSource;
    }
    @Bean(name = "entityManagerFactory")
    public LocalSessionFactoryBean entityManagerFactory() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setPackagesToScan("com.kapturecx.employeelogin.entity");
        sessionFactoryBean.setHibernateProperties(hibernateProperties());
        return sessionFactoryBean;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", hibernateDialect);
        properties.setProperty("hibernate.show_sql", hibernateShowSql);
        properties.setProperty("hibernate.format_sql", hibernateFormatSql);
        properties.setProperty("hibernate.hbm2ddl.auto", hibernateHbm2ddlAuto);
        return properties;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager hibernateTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    @Primary
    @Qualifier("employee")
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> loggingFilter(){
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthenticationFilter());
        registrationBean.addUrlPatterns("/api/auth/*");

        return registrationBean;
    }
}
