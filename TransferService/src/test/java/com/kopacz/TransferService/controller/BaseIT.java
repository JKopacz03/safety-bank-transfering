package com.kopacz.TransferService.controller;

import lombok.Getter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class BaseIT {

    @Getter
    @Container
    private static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine3.18")
                    .withDatabaseName("transfer")
                    .withPassword("qwerty")
                    .withUsername("postgres");
    @Container
    public static final RabbitMQContainer container = new RabbitMQContainer("rabbitmq:3.8-management-alpine")
            .withQueue("constraints-queue")
            .withQueue("constraints-back-queue")
            .withEnv("RABBITMQ_DEFAULT_TRANSFER", "transfer-app");

    @DynamicPropertySource
        public static void containerConfig(DynamicPropertyRegistry registry){
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
            registry.add("spring.rabbitmq.host", container::getHost);
            registry.add("spring.rabbitmq.port", container::getAmqpPort);
            registry.add("spring.rabbitmq.username", container::getAdminUsername);
            registry.add("spring.rabbitmq.password", container::getAdminPassword);
    }

}
