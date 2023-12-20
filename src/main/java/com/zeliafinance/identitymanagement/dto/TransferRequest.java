package com.zeliafinance.identitymanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferRequest {
    private String sourceAccountNumber;
    private String beneficiaryAccountNumber;
    private double amount;
    private String bankName;
    private String accountName;
    private boolean saveBeneficiary;
}
