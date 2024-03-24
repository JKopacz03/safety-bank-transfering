package com.kopacz.ConstraintsService.junit;

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
import java.time.*;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class ConstraintsServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Clock clock;
    @InjectMocks
    private ConstraintsService constraintsService;
    private Clock fixedClock;

    @Test
    void validate_allOk_returnsOk(){
        //given
        fixedClock = Clock.fixed(LocalDate.of(2024,3,20).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

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
        fixedClock = Clock.fixed(LocalDate.of(2024,3,20).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

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
        fixedClock = Clock.fixed(LocalDate.of(2024,3,23).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        Transfer transfer = new Transfer(BigDecimal.valueOf(100.00), Currency.PLN);
        //when
        String result = constraintsService.validate(transfer);
        //then
        Assertions.assertEquals("TRANSFER_IN_WEEKEND_CONSTRAINT", result);
    }
}
