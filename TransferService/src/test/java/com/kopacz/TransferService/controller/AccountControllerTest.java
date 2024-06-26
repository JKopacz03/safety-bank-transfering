package com.kopacz.TransferService.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopacz.TransferService.model.command.TransferCommand;
import com.kopacz.TransferService.model.command.TransferConstraintsCommand;
import com.kopacz.TransferService.model.dto.TransferConstraintsDto;
import com.kopacz.TransferService.model.dto.TransferDto;
import com.kopacz.TransferService.model.enums.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class AccountControllerTest extends BaseIT{
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private RabbitTemplate rabbitTemplate;
    @Autowired
    public AccountControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void secondTransferShouldntSucceedCauseThenAccountBalanceIsNegative() throws Exception {
        Mono<String> transfer1 = sendTransferRequest()
                .onErrorResume(WebClientResponseException.BadRequest.class, ex -> Mono.empty());

        Mono<String> transfer2 = sendTransferRequest()
                .onErrorResume(WebClientResponseException.BadRequest.class, ex -> Mono.empty());

        Mono.zip(transfer1, transfer2)
                .doOnNext(tuple -> {
                        throw new IllegalStateException("Both transfers returned successfully, expected at least one to fail.");
                })
                .block();
    }

    public Mono<String> sendTransferRequest() {
        WebClient webClient = WebClient.create("http://localhost:8081");
        TransferCommand transferCommand = new TransferCommand(new BigDecimal("700"), 1001L, 1002L);

        return webClient.post()
                .uri("/api/v1")
                .headers(headers -> headers.setBasicAuth("user1", "qwerty"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(transferCommand), TransferCommand.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    

    @Test
    @WithMockUser(username = "user1")
    void shouldTransfer() throws Exception {
        TransferCommand transferCommand = new TransferCommand(new BigDecimal("100"), 1001L, 1002L);
        String json = objectMapper.writeValueAsString(transferCommand);

        doAnswer(e -> {
            rabbitTemplate.convertAndSend("constraints-back-queue", new TransferConstraintsCommand(
                    transferCommand.getFromAccount(),
                    transferCommand.getToAccount(),
                    transferCommand.getAmount(),
                    Currency.PLN,
                    "OK"
            ));
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), any(TransferConstraintsDto.class));

        mockMvc.perform(post("/api/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1")
    void invalidAmount_shouldReturnBadRequest() throws Exception {
        TransferCommand transferCommand = new TransferCommand(new BigDecimal("-10"), 1001L, 1002L);
        String json = objectMapper.writeValueAsString(transferCommand);

        mockMvc.perform(post("/api/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("missing amount"))
                .andExpect(jsonPath("$.uri").value("/api/v1"))
                .andExpect(jsonPath("$.method").value("POST"));
    }

    @Test
    @WithMockUser(username = "user1")
    void invalidCurrencyFrom_shouldReturnBadRequest() throws Exception {
        TransferCommand transferCommand = new TransferCommand(new BigDecimal("100"), -1001L, 1002L);
        String json = objectMapper.writeValueAsString(transferCommand);

        mockMvc.perform(post("/api/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("number of account from cannot be negative"))
                .andExpect(jsonPath("$.uri").value("/api/v1"))
                .andExpect(jsonPath("$.method").value("POST"));
    }

    @Test
    @WithMockUser(username = "user1")
    void invalidCurrencyTo_shouldReturnBadRequest() throws Exception {
        TransferCommand transferCommand = new TransferCommand(new BigDecimal("100"), 1001L, -1002L);
        String json = objectMapper.writeValueAsString(transferCommand);

        mockMvc.perform(post("/api/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("number of account to cannot be negative"))
                .andExpect(jsonPath("$.uri").value("/api/v1"))
                .andExpect(jsonPath("$.method").value("POST"));
    }
}
