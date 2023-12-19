package com.zeliafinance.identitymanagement.loanRepayment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepaymentData {
    private int monthCount;
    private double expectedAmount;
    private double amountPaid;
    private String repaymentStatus;
    private double interest;
    private String repaymentDate;
}
