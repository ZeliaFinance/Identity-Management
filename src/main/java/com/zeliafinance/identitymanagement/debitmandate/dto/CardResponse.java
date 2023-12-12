package com.zeliafinance.identitymanagement.debitmandate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardResponse {
    private Long id;
    private String walletId;
    private String bin;
    private String lastFour;
    private String authCode;
    private String cardExpiry;
    private String cardType;
}
