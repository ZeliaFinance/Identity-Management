package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorRequest;
import org.springframework.http.ResponseEntity;

public interface LoanCalculatorService {

    ResponseEntity<CustomResponse> applyForLoan(LoanApplicationRequest request);
    ResponseEntity<CustomResponse> calculateLoan(LoanCalculatorRequest request);
}
