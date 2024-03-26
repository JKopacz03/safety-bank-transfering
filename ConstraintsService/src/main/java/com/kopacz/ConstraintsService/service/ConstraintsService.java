package com.kopacz.ConstraintsService.service;

import com.kopacz.ConstraintsService.model.Currency;
import com.kopacz.ConstraintsService.model.TransferCommand;
import com.kopacz.ConstraintsService.model.TransferDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ConstraintsService {
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @RabbitListener(queues = "constraints-queue")
    public void validate(TransferCommand transferCommand){
        TransferDto transferDto = new TransferDto(transferCommand.getFromAccount(),
                transferCommand.getToAccount(),
                transferCommand.getAmount(),
                transferCommand.getCurrency(),
                "OK");
        if (LocalDate.now(clock).getDayOfWeek().equals(DayOfWeek.SATURDAY)
        || LocalDate.now(clock).getDayOfWeek().equals(DayOfWeek.SUNDAY)){
            transferDto.setMess("TRANSFER_IN_WEEKEND_CONSTRAINT");
        }
        if(getAmountInEuro(transferCommand).compareTo(BigDecimal.valueOf(15000)) > 0){
            transferDto.setMess("MAX15K_EURO_CONSTRAINT");
        }
        rabbitTemplate.convertAndSend("constraints-back-queue", transferDto);
    }

    private BigDecimal getAmountInEuro(TransferCommand transferCommand) {
        if(transferCommand.getCurrency().equals(Currency.EUR)){
            return transferCommand.getAmount();
        }
        String url = "https://latest.currency-api.pages.dev/v1/currencies/eur.json";
        String currencies = restTemplate.getForObject(url, String.class);
        Pattern pattern = Pattern.compile("\"" + transferCommand.getCurrency().toString().toLowerCase() + "\":\\s(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(Objects.requireNonNull(currencies));
        matcher.find();
        String group = matcher.group(1);
        return transferCommand.getAmount().multiply(new BigDecimal(group));
    }
}
