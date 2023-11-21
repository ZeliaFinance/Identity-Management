package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingRequest;
import org.springframework.http.ResponseEntity;

public interface LoanOfferingService {
    ResponseEntity<CustomResponse> saveLoanOffering(LoanOfferingRequest loanOfferingRequest);
    ResponseEntity<CustomResponse> fetchAllLoanOfferings();
    ResponseEntity<CustomResponse> fetchLoanOfferingByProductName(String loanProduct);
}
