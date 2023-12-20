package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeneficiaryRequest {
    private String walletId;
    private String beneficiaryAccountNumber;
    private String beneficiaryBank;
    private String accountName;
}
