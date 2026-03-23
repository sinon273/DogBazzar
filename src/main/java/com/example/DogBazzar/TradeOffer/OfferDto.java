package com.example.DogBazzar.TradeOffer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record OfferDto(
        @Null
        Long id,//id оффера
        @NotNull
        Long listingId,//id объявления к которому сделано предложение
        @Null
        Long buyerId,//id пользователья который хочет купить
        @Null
        String buyerName,//имя
        @Null
        Long sellerId,//id продовца
        @Null
        String sellerName,// имя
        @Null
        Long dogId,// номер собаки
        @Null
        String dogName,//имя собаки
        @Null
        BigDecimal offer,//за сколько хочет купить
        String message,// сообщение
        @Null
        TradeOfferStatus status,// статус оффера(Pending)
        @Null
        LocalDateTime createdAt,//дата создания оффера(текущее время)
        @Null
        LocalDateTime respondedAt//время ответа на оффер(null)

) {

}
