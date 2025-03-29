package com.br.arlei.carga_cliente.data_source;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {


    // ####### BANCO BATHC (H2) #######
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    DataSource datasource() {
        return DataSourceBuilder.create().build();
    }


    // ####### BANCO POSTGRES #######

    @Bean(name = DataSourceQualifiers.POSTGRES_DATA_SOURCE)
    @ConfigurationProperties(prefix = "postgres.datasource")
    DataSource postgresDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = JdbcQualifiers.POSTGRES_JDBC_CLIENT)
    JdbcClient postgresJdbcClient(
            @Qualifier(DataSourceQualifiers.POSTGRES_DATA_SOURCE)
            DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }


    // ####### BANCO MYSQL #######

    @Bean(name = DataSourceQualifiers.MYSQL_DATA_SOURCE)
    @ConfigurationProperties(prefix = "mysql.datasource")
    DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = JdbcQualifiers.MYSQL_JDBC_CLIENT)
    JdbcClient mysqlJdbcClient(
            @Qualifier(DataSourceQualifiers.MYSQL_DATA_SOURCE)
            DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }


//    @Bean
//    public JobRepository jobRepository(@Qualifier("springDatasource") DataSource h2DataSource,
//                                       PlatformTransactionManager transactionManager) throws Exception {
//        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
//        factoryBean.setDataSource(h2DataSource); // Define o DataSource do H2
//        factoryBean.setTransactionManager(transactionManager);
//        factoryBean.setDatabaseType("H2");
//        factoryBean.afterPropertiesSet();
//        return factoryBean.getObject();
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(@Qualifier("springDatasource") DataSource h2DataSource) {
//        return new DataSourceTransactionManager(h2DataSource);
//    }

}