package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import org.springframework.http.ResponseEntity;

public interface LoanApplicationService {
    ResponseEntity<CustomResponse> stageOne(LoanApplicationRequest request);
    ResponseEntity<CustomResponse> stageTwo(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> stageThree(String loanRefNo, LoanApplicationRequest loanApplicationRequest) throws Exception;
    ResponseEntity<CustomResponse> stageFour(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> stageFive(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> fetchAllLoanApplications();
    ResponseEntity<CustomResponse> loanApplicationHistory();
    ResponseEntity<CustomResponse> viewLoanApplicationsByStatus(String loanApplicationStatus);
    ResponseEntity<CustomResponse> updateStageOne(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> searchByPhoneNumber(String phoneNumber);
    ResponseEntity<CustomResponse> searchByLoanAppStatus(String loanApplicationStatus);
}
