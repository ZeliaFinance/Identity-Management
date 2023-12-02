package com.zeliafinance.identitymanagement.debitmandate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String walletId;
    private String cvv;
    private String bin;
    private String lastFour;
    private String authCode;
    private LocalDate cardExpiry;
    private String cardType;
}
