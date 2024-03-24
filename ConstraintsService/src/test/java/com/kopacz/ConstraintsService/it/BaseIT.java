package com.kopacz.ConstraintsService.it;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class BaseIT {

    @Container
    public static final RabbitMQContainer container = new RabbitMQContainer("rabbitmq:3.8-management-alpine")
            .withQueue("constraints-queue")
            .withEnv("RABBITMQ_DEFAULT_CONSTRAINTS", "constraints-app");

    @DynamicPropertySource
    public static void containerConfig(DynamicPropertyRegistry registry){
        registry.add("spring.rabbitmq.host", container::getHost);
        registry.add("spring.rabbitmq.port", container::getAmqpPort);
        registry.add("spring.rabbitmq.username", container::getAdminUsername);
        registry.add("spring.rabbitmq.password", container::getAdminPassword);
    }

}
