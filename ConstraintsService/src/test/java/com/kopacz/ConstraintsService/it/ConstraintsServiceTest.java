package com.kopacz.ConstraintsService.it;

import com.kopacz.ConstraintsService.model.Currency;
import com.kopacz.ConstraintsService.model.TransferCommand;
import com.kopacz.ConstraintsService.model.TransferDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class ConstraintsServiceTest extends BaseIT{
    private final RabbitTemplate rabbitTemplate;
    @MockBean
    private Clock clock;
    private Clock fixedClock;
    private String mess;

    @Autowired
    public ConstraintsServiceTest(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Test
    void shouldReturnsOk() throws InterruptedException {
        fixedClock = Clock.fixed(LocalDate.of(2024,3,20).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        TransferCommand transferCommand = new TransferCommand(1001L, 1002L, BigDecimal.valueOf(100), Currency.PLN);
        rabbitTemplate.convertAndSend("constraints-queue", transferCommand);

        Thread.sleep(1000);
        Assertions.assertEquals("OK", mess);
    }

    @Test
    void shouldReturnsTRANSFER_IN_WEEKEND_CONSTRAINT() throws InterruptedException {
        fixedClock = Clock.fixed(LocalDate.of(2024,3,23).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        TransferCommand transferCommand = new TransferCommand(1001L, 1002L, BigDecimal.valueOf(100), Currency.PLN);
        rabbitTemplate.convertAndSend("constraints-queue", transferCommand);

        Thread.sleep(1000);
        Assertions.assertEquals("TRANSFER_IN_WEEKEND_CONSTRAINT", mess);
    }
    @Test
    void shouldReturnsMAX15K_EURO_CONSTRAINT() throws InterruptedException {
        fixedClock = Clock.fixed(LocalDate.of(2024,3,20).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        TransferCommand transferCommand = new TransferCommand(1001L, 1002L, BigDecimal.valueOf(70000), Currency.PLN);
        rabbitTemplate.convertAndSend("constraints-queue", transferCommand);

        Thread.sleep(1000);
        Assertions.assertEquals("MAX15K_EURO_CONSTRAINT", mess);
    }

    @RabbitListener(queues = "constraints-back-queue")
    public void getResponse(TransferDto transferDto){
        fixedClock = Clock.fixed(LocalDate.of(2024,3,20).atStartOfDay(
                ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        mess = transferDto.getMess();
    }
}
