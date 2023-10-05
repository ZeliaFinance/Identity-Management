package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanProductRequest;
import org.springframework.http.ResponseEntity;

public interface LoanProductService {

    ResponseEntity<CustomResponse> saveLoanProduct(LoanProductRequest request);
    ResponseEntity<CustomResponse> fetchAllLoanProducts();
    ResponseEntity<CustomResponse> fetchLoanProductByProductName(String loanProductName) throws Exception;
    ResponseEntity<CustomResponse> fetchLoanProductById(Long productId) throws Exception;
    ResponseEntity<CustomResponse> updateLoanProduct(LoanProductRequest request, Long productId) throws Exception;
    ResponseEntity<CustomResponse> deleteLoanProduct(Long productId) throws Exception;
}
