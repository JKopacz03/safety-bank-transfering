package com.kopacz.TransferService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue currencyRatesQueue() {
        return new Queue("constraints-queue");
    }

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange("transfer");
    }

    @Bean
    public Binding jsonBinding(){
        return BindingBuilder
                .bind(currencyRatesQueue())
                .to(exchange())
                .with("constraints_rk");
    }

    @Bean
    public Jackson2JsonMessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
