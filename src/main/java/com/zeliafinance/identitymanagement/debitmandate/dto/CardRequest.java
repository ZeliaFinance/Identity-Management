package com.zeliafinance.identitymanagement.debitmandate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardRequest {
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear;
    private String cardType;
}
