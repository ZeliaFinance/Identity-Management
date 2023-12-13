package com.zeliafinance.identitymanagement.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {
    private UserInfo userInfo;
    private double amountDisbursed;
    private double totalRepayments;
    private double interest;
    private double principal;
    private List<LoanDisbursal> loanDisbursal;
    private List<Repayments> loanRepayments;
}
