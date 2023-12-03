package com.zeliafinance.identitymanagement.loanRepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepaymentData {
    private int monthCount;
    private double expectedAmount;
    private double amountPaid;
}
