package com.zeliafinance.identitymanagement.loanDisbursal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisbursalRequest {
    private String walletId;
    private String loanRefNo;
    private double appliedAmount;
    private double amountDisbursed;
    private String dateDisbursed;
    private String disbursedBy;
    private double amountToPayBack;
}
