package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface LoanApplicationService {
    ResponseEntity<CustomResponse> stageOne(LoanApplicationRequest request);
    ResponseEntity<CustomResponse> stageTwo(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> stageThree(String file1, String file2, String loanRefNo, LoanApplicationRequest loanApplicationRequest) throws Exception;
    ResponseEntity<CustomResponse> stageFour(Optional<MultipartFile> file1, Optional<MultipartFile> file2, String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> stageFive(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> fetchAllLoanApplications();
    ResponseEntity<CustomResponse> loanApplicationHistory();
    ResponseEntity<CustomResponse> viewLoanApplicationsByStatus(String loanApplicationStatus);
    ResponseEntity<CustomResponse> updateStageOne(String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> searchByPhoneNumber(String phoneNumber);
    ResponseEntity<CustomResponse> searchByLoanAppStatus(String loanApplicationStatus);
}
