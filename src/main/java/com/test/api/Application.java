package com.test.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* Class description
*/
@MapperScan("com.test.api.mapper")
@SpringBootApplication
public class Application {

	public static void main(String[] args) {

		// TODO Auto-generated method stub
		SpringApplication.run(Application.class, args);

	}

}
