package com.zeliafinance.identitymanagement.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanCalculatorResponse {
    private double amountToPayBack;
    private double monthlyRepayment;
    private double interestRate;
}
