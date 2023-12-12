package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LoanOfferingService {
    ResponseEntity<CustomResponse> saveLoanOffering(LoanOfferingRequest loanOfferingRequest);
    ResponseEntity<CustomResponse> fetchAllLoanOfferings();
    List<LoanOfferingResponse> fetchLoanOfferingByProductName(String loanProduct);
}
