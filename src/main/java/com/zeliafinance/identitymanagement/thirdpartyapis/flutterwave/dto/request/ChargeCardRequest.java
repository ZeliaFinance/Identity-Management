package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeCardRequest {
    private int amount;
    private long cardNumber;
    private int cvv;
    private int expiryMonth;
    private int expiryYear;
    private String email;
    @JsonProperty("tx_ref")
    private String txRef;




}
