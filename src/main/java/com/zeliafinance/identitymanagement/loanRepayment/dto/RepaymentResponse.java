package com.zeliafinance.identitymanagement.loanRepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RepaymentResponse {
    private int repaymentMonths;
    private String repaymentStatus;
    private long userId;
    private String loanType;
    private int loanTenor;
    private double monthlyRepayment;
    private String nextRepayment;
    private double amountPaid;
    private List<RepaymentData> repaymentData;
    private double interest;
    private double interestRate;
    private String walletId;
    private double principal;
}
