package com.zeliafinance.identitymanagement.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequest {
    private BigDecimal loanAmount;
    private String companyName;
    private String companyAddress;
    private String cacRegistration;
    private String businessAccountNumber;
    private String loanPurpose;
    private String loanType;
    private String walletId;
}
