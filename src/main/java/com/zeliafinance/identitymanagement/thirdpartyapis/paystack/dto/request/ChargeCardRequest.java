package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeCardRequest {
    private String authorization_code;
    private String email;
    private String amount;
    private String otp;
    private String pin;
}
