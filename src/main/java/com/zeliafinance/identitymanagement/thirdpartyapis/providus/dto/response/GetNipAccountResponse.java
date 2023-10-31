package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetNipAccountResponse {
    private String bankCode;
    private String accountName;
    private String transactionReference;
    private String bvn;
    private String responseMessage;
    private String responseCode;
}
