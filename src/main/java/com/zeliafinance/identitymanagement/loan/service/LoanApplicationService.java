package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface LoanApplicationService {
    ResponseEntity<CustomResponse> stageOne(LoanApplicationRequest request);
    ResponseEntity<CustomResponse> stageTwo(Optional<MultipartFile> multipartFile, String loanRefNo, LoanApplicationRequest request) throws Exception;
    ResponseEntity<CustomResponse> stageThree(Optional<MultipartFile> file1, Optional<MultipartFile> file2, String loanRefNo, LoanApplicationRequest loanApplicationRequest) throws Exception;
}
