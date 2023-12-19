package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeCardResponse {
    private boolean status;
    private String message;
    private ResponseData data;
    private Customer customer;
    private Object plan;
    private Long id;
}
