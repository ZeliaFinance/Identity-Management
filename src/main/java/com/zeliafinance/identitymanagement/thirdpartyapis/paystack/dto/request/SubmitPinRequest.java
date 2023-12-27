package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmitPinRequest {
    private String pin;
    private String reference;
}
