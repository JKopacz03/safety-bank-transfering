package com.kopacz.ConstraintsService.service;

import com.kopacz.ConstraintsService.model.Currency;
import com.kopacz.ConstraintsService.model.Transfer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ConstraintsService {
    private final RestTemplate restTemplate;

    @RabbitListener(queues = "constraints-queue")
    public String validate(Transfer transfer){
        if (LocalDateTime.now().getDayOfWeek().equals(DayOfWeek.SATURDAY)
        || LocalDateTime.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)){
            return "TRANSFER_IN_WEEKEND_CONSTRAINT";
        }
        if(getAmountInEuro(transfer).compareTo(BigDecimal.valueOf(15000)) > 0){
            return "MAX15K_EURO_CONSTRAINT";
        }
        return "OK";
    }

    private BigDecimal getAmountInEuro(Transfer transfer) {
        if(transfer.getCurrency().equals(Currency.EUR)){
            return transfer.getAmount();
        }
        String url = "https://latest.currency-api.pages.dev/v1/currencies/eur.json";
        String currencies = restTemplate.getForObject(url, String.class);
        Pattern pattern = Pattern.compile("\"" + transfer.getCurrency().toString().toLowerCase() + "\":\\s(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(Objects.requireNonNull(currencies));
        matcher.find();
        String group = matcher.group(1);
        return transfer.getAmount().multiply(new BigDecimal(group));
    }
}
