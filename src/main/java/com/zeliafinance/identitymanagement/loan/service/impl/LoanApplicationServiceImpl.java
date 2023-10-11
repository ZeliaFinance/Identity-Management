package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.entity.LoanProduct;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.repository.LoanProductRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.impl.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zeliafinance.identitymanagement.utils.AccountUtils.*;
@Service
@AllArgsConstructor
@Slf4j
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private LoanProductRepository loanProductRepository;
    private UserCredentialRepository userCredentialRepository;
    private LoanApplicationRepository loanApplicationRepository;
    private CustomUserDetailsService customUserDetailsService;
    @Override
    public ResponseEntity<CustomResponse> applyForLoan(LoanApplicationRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();

        boolean isWalletIdExists = loanApplicationRepository.findByWalletId(walletId).isPresent();
        if (isWalletIdExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(PENDING_LOAN_MESSAGE)
                    .build());
        }

        LoanApplication loanApplication = LoanApplication.builder()
                .loanAmount(request.getLoanAmount())
                .walletId(walletId)
                .loanAmount(request.getLoanAmount())
                .loanTenor(request.getLoanTenor())
                .companyName(request.getCompanyName())
                .companyAddress(request.getCompanyAddress())
                .companyEmailAddress(request.getCompanyEmailAddress())
                .cacRegistration(request.getCacRegistration())
                .businessAccount(request.getBusinessAccountNumber())
                .loanPurpose(request.getLoanPurpose())
                .createdBy(email)
                .modifiedBy(email)
                .build();

        LoanApplication savedLoanApplication = loanApplicationRepository.save(loanApplication);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .responseMessage(LOAN_APPLICATION_SUCCESS)
                        .responseBody(savedLoanApplication)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> calculateLoan(LoanApplicationRequest request) {

        BigDecimal amountToPay = BigDecimal.ZERO;
        List<LoanProduct> loanProducts = loanProductRepository.findAll().stream().filter(loanProduct -> loanProduct.getLoanProductName().equalsIgnoreCase(request.getLoanType())).toList();
        for (LoanProduct loanProduct : loanProducts){
            if (request.getLoanAmount().doubleValue() >= loanProduct.getMinAmount().doubleValue() && request.getLoanAmount().doubleValue() <= loanProduct.getMaxAmount().doubleValue() && request.getLoanTenor() >= loanProduct.getMinDuration() && request.getLoanTenor() <= loanProduct.getMaxDuration()){
                log.info("Interest rate: {}", loanProduct.getInterestRate());
                log.info("Id of loan product: {}", loanProduct.getId());
                amountToPay = request.getLoanAmount().add(BigDecimal.valueOf((request.getLoanAmount().doubleValue() * ((double)loanProduct.getInterestRate() / 100))/100));
            }

        }

        Map<String, BigDecimal> dataMap = new HashMap<>();
        dataMap.put("amountToPayBack", amountToPay);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(dataMap)
                .build());

    }
}
