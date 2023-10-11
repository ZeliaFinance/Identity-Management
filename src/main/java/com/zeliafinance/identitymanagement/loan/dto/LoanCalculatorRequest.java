package com.zeliafinance.identitymanagement.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanCalculatorRequest {
    private double loanAmount;
    private String loanType;
    private int loanTenor;
}
