package com.kopacz.ConstraintsService.it;

import com.kopacz.ConstraintsService.model.Currency;
import com.kopacz.ConstraintsService.model.Transfer;
import com.kopacz.ConstraintsService.service.ConstraintsService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.Mockito.doReturn;

public class ConstraintsServiceTest extends BaseIT{
    private final RabbitTemplate rabbitTemplate;
    @MockBean
    private Clock clock;
    private Clock fixedClock;

    @Autowired
    public ConstraintsServiceTest(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Test
    void shouldReturnsOk(){
        fixedClock = Clock.fixed(LocalDate.of(2024,3,20).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        Transfer transfer = new Transfer(BigDecimal.valueOf(100), Currency.PLN);
        String receive = (String) rabbitTemplate.convertSendAndReceive("constraints-queue", transfer);
        Assertions.assertEquals("OK", receive);
    }

    @Test
    void shouldReturnsTRANSFER_IN_WEEKEND_CONSTRAINT(){
        fixedClock = Clock.fixed(LocalDate.of(2024,3,23).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        Transfer transfer = new Transfer(BigDecimal.valueOf(100), Currency.PLN);
        String receive = (String) rabbitTemplate.convertSendAndReceive("constraints-queue", transfer);
        Assertions.assertEquals("TRANSFER_IN_WEEKEND_CONSTRAINT", receive);
    }
    @Test
    void shouldReturnsMAX15K_EURO_CONSTRAINT(){
        fixedClock = Clock.fixed(LocalDate.of(2024,3,20).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        Transfer transfer = new Transfer(BigDecimal.valueOf(70000), Currency.PLN);
        String receive = (String) rabbitTemplate.convertSendAndReceive("constraints-queue", transfer);
        Assertions.assertEquals("MAX15K_EURO_CONSTRAINT", receive);
    }

}
