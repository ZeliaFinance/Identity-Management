package com.zeliafinance.identitymanagement.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanProductRequest {
    private String loanProductName;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minDuration;
    private Integer maxDuration;
    private Integer interestRate;
}
