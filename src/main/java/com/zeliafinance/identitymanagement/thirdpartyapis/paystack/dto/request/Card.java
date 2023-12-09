package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card {
    private String cvv;
    private String number;
    private String expiry_month;
    private String expiry_year;
}