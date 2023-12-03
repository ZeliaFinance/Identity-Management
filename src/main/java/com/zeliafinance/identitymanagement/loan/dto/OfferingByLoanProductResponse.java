package com.zeliafinance.identitymanagement.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfferingByLoanProductResponse {
    private String loanProduct;
    private double minAmount;
    private double maxAmount;
    private double interestRate;
    private List<Integer> loanBrackets;
}
