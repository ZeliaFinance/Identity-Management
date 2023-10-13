package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.response;

import com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.ChargeResponseData;
import com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.Meta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeCardResponse {
    private String status;
    private String message;
    private ChargeResponseData data;
    private Meta meta;
}
