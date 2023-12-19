package com.zeliafinance.identitymanagement.loanRepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayOffLoanRequest {
    private String channel;
    private String loanRefNo;
}
