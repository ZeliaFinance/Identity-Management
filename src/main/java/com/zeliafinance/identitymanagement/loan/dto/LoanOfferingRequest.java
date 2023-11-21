package com.zeliafinance.identitymanagement.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanOfferingRequest {
    private String loanProduct;
    private int daysAvailable;
    private double interestRate;
    private double minAmount;
    private double maxAmount;
}
