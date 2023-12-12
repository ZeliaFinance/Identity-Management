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
public class LoanProductRequest {
    private String loanProductName;
    private double minAmount;
    private double maxAmount;
    private int minDuration;
    private int maxDuration;
    private double interestRate;
    private List<LoanOfferingResponse> loanOfferingResponseList;
}
