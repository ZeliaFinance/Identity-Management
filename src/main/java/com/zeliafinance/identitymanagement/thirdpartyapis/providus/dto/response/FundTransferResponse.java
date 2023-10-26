package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundTransferResponse {
    private String transactionReference;
    private String sessionId;
    private String responseMessage;
    private String responseCode;
}
