package com.zeliafinance.identitymanagement.loan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanOfferingResponse {
    private String loanProduct;
    private double interestRate;
    private List<Integer> bracket;
    private int daysAvailable;
    private double minAmount;
    private double maxAmount;

    public LoanOfferingResponse(String s, Double aDouble, List<Integer> daysAvailable, Double aDouble1, Double aDouble2) {
        loanProduct = s;
        interestRate = aDouble;
        bracket = daysAvailable;
        minAmount = aDouble1;
        maxAmount = aDouble2;
    }
}
