package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;

public class PersonalLoanApplicationService implements LoanApplicationService {

    @Override
    public ResponseEntity<CustomResponse> applyForLoan(LoanApplicationRequest request) {
        return null;
    }
}
