package com.kopacz.ConstraintsService;

import com.kopacz.ConstraintsService.model.Currency;
import com.kopacz.ConstraintsService.model.Transfer;
import com.kopacz.ConstraintsService.service.ConstraintsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class ConstraintsServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private ConstraintsService constraintsService;

    @Test
    void validate_allOk_returnsOk(){
        //given
        Transfer transfer = new Transfer(BigDecimal.valueOf(100.00), Currency.PLN);

        String url = "https://latest.currency-api.pages.dev/v1/currencies/eur.json";
        Mockito.when(restTemplate.getForObject(url, String.class)).thenReturn("\"pln\": 4.3072176");
        //when
        String result = constraintsService.validate(transfer);
        //then
        Assertions.assertEquals("OK", result);
    }

    @Test
    void validate_amountBiggerThen15EuroFromPln_returnMAX15K_EURO_CONSTRAINT(){
        //given
        Transfer transfer = new Transfer(BigDecimal.valueOf(70000.00), Currency.PLN);

        String url = "https://latest.currency-api.pages.dev/v1/currencies/eur.json";
        Mockito.when(restTemplate.getForObject(url, String.class)).thenReturn("\"pln\": 4.3072176");
        //when
        String result = constraintsService.validate(transfer);
        //then
        Assertions.assertEquals("MAX15K_EURO_CONSTRAINT", result);
    }

    @Test
    void validate_transferInWeekend_returnTRANSFER_IN_WEEKEND_CONSTRAINT(){
        //given
        Transfer transfer = new Transfer(BigDecimal.valueOf(100.00), Currency.PLN);

        MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class);
        mockedStatic.when(LocalDateTime::now).thenReturn(LocalDateTime.of(23,3,2024,12,0,0));
        //when
        String result = constraintsService.validate(transfer);
        //then
        Assertions.assertEquals("TRANSFER_IN_WEEKEND_CONSTRAINT", result);
    }
}
