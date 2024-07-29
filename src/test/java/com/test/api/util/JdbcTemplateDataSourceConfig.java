//package com.test.api.util;
//import com.alibaba.druid.pool.DruidDataSource;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//import javax.sql.DataSource;
///**
// * <p>
// *
// * </p>
// *
// * @author Faisal Mulya Santosa
// * @date
// */
//@Configuration
//public class JdbcTemplateDataSourceConfig {
//    @Bean(name = "primaryDataSource")
//    public DataSource primaryDataSource() {
//        DataSource dataSource = new DriverDataSource();
//        dataSource.setUrl("jdbc:mysql://192.168.114.101:3306");
//        dataSource.setUsername("john");
//        dataSource.setPassword("ksOHn5SUTSRF^blc");
//        return dataSource;
//    }
//    @Bean(name="primaryJdbcTemplate")
//    public JdbcTemplate primaryJdbcTemplate (
//            @Qualifier("primaryDataSource")  DataSource dataSource ) {
//        return new JdbcTemplate(dataSource);
//    }
//}
