package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmeLoanApplicationServiceImpl implements LoanApplicationService {


    private final LoanApplicationRepository loanApplicationRepository;
    private final UserCredentialRepository userCredentialRepository;

    public SmeLoanApplicationServiceImpl(LoanApplicationRepository loanApplicationRepository,
                                         UserCredentialRepository userCredentialRepository){
        this.loanApplicationRepository = loanApplicationRepository;
        this.userCredentialRepository = userCredentialRepository;
    }
    @Override
    public ResponseEntity<CustomResponse> applyForLoan(LoanApplicationRequest request) throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();
//        LoanApplication pendingLoanApplication = loanApplicationRepository.findByWalletId(walletId).get();
//        if (pendingLoanApplication.getWalletId() != null && pendingLoanApplication.getLoanStatus().equalsIgnoreCase("PENDING")){
//            return ResponseEntity.badRequest().body(CustomResponse.builder()
//                            .statusCode(HttpStatus.BAD_REQUEST.value())
//                            .responseMessage(AccountUtils.PENDING_LOAN_MESSAGE)
//                    .build());
//        }

        LoanApplication loanApplication = LoanApplication.builder()
                .walletId(walletId)
                .loanAmount(request.getLoanAmount())
                .companyName(request.getCompanyName())
                .companyAddress(request.getCompanyAddress())
                .companyEmailAddress(request.getCompanyEmailAddress())
                .cacRegistration(request.getCacRegistration())
                .businessAccount(request.getBusinessAccountNumber())
                .loanPurpose(request.getLoanPurpose())
                .loanType(request.getLoanType())
                .loanStatus("PENDING")
                .build();

        loanApplicationRepository.save(loanApplication);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanApplication)
                .build());



    }
}
