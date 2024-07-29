package com.test.api.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-10-07 15:33
 * @Description:
 */
@Configuration
public class RabbitConfig {
    @Bean("fatConnectionFactory")
    public ConnectionFactory fatConnectionFactory(
            @Value("${spring.rabbitmq.fat.host}") String    host,
            @Value("${spring.rabbitmq.fat.port}") int    port,
            @Value("${spring.rabbitmq.fat.username}") String    username,
            @Value("${spring.rabbitmq.fat.password}") String    password
    ){
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        return factory;
    }
    @Bean("fatRabbitTemplate")
    public RabbitTemplate fatRabbitTemplate(@Qualifier("fatConnectionFactory") ConnectionFactory fatConnectionFactory){
        RabbitTemplate fatRabbitTemplate = new RabbitTemplate(fatConnectionFactory);
        return fatRabbitTemplate;
    }
    @Bean("devRabbitTemplate")
    public RabbitTemplate devRabbitTemplate( @Value("${spring.rabbitmq.dev.host}") String    host,
                                             @Value("${spring.rabbitmq.dev.port}") int    port,
                                             @Value("${spring.rabbitmq.dev.username}") String    username,
                                             @Value("${spring.rabbitmq.dev.password}") String    password){
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        RabbitTemplate devRabbitTemplate = new RabbitTemplate(factory);
        return devRabbitTemplate;
    }
}
