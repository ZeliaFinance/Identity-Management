package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.otp.dto.OtpValidationRequest;
import org.springframework.http.ResponseEntity;

public interface LoanApplicationService {
    ResponseEntity<CustomResponse> stageOne(LoanApplicationRequest request);
    ResponseEntity<CustomResponse> stageTwo(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> stageThree(String loanRefNo, LoanApplicationRequest loanApplicationRequest) throws Exception;
    ResponseEntity<CustomResponse> stageFour(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> stageFive(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> initiateCosignerVerification(String loanRefNo);
    ResponseEntity<CustomResponse> validateCosignerEmail(String loanRefNo, OtpValidationRequest otpValidationRequest);
    ResponseEntity<CustomResponse> fetchAllLoanApplications(int pageNo, int pageSize);
    ResponseEntity<CustomResponse> loanApplicationHistory(int pageNo, int pageSize);
    ResponseEntity<CustomResponse> viewLoanApplicationsByStatus(String loanApplicationStatus, int pageNo, int pageSize);
    ResponseEntity<CustomResponse> updateStageOne(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> searchByPhoneNumber(String phoneNumber);
    ResponseEntity<CustomResponse> searchByLoanAppStatus(String loanApplicationStatus);
    ResponseEntity<CustomResponse> deleteLoan(Long loanId);
    ResponseEntity<CustomResponse> cancelLoan(String loanRefNo);
    ResponseEntity<CustomResponse> fetchByLoanRefNo(String loanRefNo);
    ResponseEntity<CustomResponse> approveLoan(String loanRefNo);
    ResponseEntity<CustomResponse> denyLoan(String loanRefNo);
}
