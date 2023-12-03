package com.zeliafinance.identitymanagement.loanRepayment.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class RepaymentService {
    private final RepaymentsRepository repaymentsRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final ModelMapper modelMapper;

    public ResponseEntity<CustomResponse> userRepaymentHistory(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Logged in user: {}", email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();
        log.info("User wallet Id: {}", walletId);
        List<Repayments> repayments = repaymentsRepository.findByWalletId(walletId);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(repayments)
                .build());
    }
}
