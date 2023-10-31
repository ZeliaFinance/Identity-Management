package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundTransferRequest {
    private String beneficiaryAccountName;
    private double transactionAmount;
    private String currencyCode;
    private String narration;
    private String sourceAccountName;
    private String sourceAccountNumber;
    private String beneficiaryBank;
    private String transactionReference;
    private String userName;
    private String password;
}
