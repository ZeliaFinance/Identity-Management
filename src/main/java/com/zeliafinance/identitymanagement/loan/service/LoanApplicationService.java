package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import org.springframework.http.ResponseEntity;

public interface LoanApplicationService {

    ResponseEntity<CustomResponse> applyForLoan(LoanApplicationRequest request);
    ResponseEntity<CustomResponse> calculateLoan(LoanApplicationRequest request);
}
