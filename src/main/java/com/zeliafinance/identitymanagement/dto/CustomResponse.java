package com.zeliafinance.identitymanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingResponse;
import com.zeliafinance.identitymanagement.loanDisbursal.dto.DisbursalRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResponse{
    private int statusCode;
    private String responseMessage;
    private Object responseBody;
    private String token;
    private String hashedPassword;
    private String hashedPin;
    private Info info;
    private Boolean otpStatus;
    private String referenceId;
    private String expiry;
    private LoanCalculatorResponse loanCalculatorResponse;
    private Boolean pinVerificationStatus;
    private Boolean minInterestRate;
    private LoanOfferingResponse loanOfferingResponse;
    private DisbursalRequest disbursalRequest;
    private Boolean canApplyForLoan;
}
