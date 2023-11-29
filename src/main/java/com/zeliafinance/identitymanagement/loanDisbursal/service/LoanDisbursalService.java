package com.zeliafinance.identitymanagement.loanDisbursal.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.entity.Transactions;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorResponse;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanCalculatorService;
import com.zeliafinance.identitymanagement.loanDisbursal.dto.DisbursalRequest;
import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import com.zeliafinance.identitymanagement.loanDisbursal.repository.LoanDisbursalRepository;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.impl.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.zeliafinance.identitymanagement.utils.AccountUtils.*;

@Service
@AllArgsConstructor
@Slf4j
public class LoanDisbursalService {

    private final LoanDisbursalRepository loanDisbursalRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final ModelMapper modelMapper;
    private final LoanApplicationRepository loanApplicationRepository;
    private final RepaymentsRepository repaymentsRepository;
    private final LoanCalculatorService loanCalculatorService;
    private final TransactionService transactionService;

    public ResponseEntity<CustomResponse> disburseLoan(DisbursalRequest request){
        //check for double disbursements
        LoanDisbursal checkingDisbursal = loanDisbursalRepository.findByLoanRefNo(request.getLoanRefNo());
        if (checkingDisbursal != null){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(DUPLICATE_DISBURSAL_ATTEMPT)
                    .build());
        }
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(request.getLoanRefNo()).get();
        String walletId = loanApplication.getWalletId();

        log.info("Loan Application: {}", walletId);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        //loanapplication -> walletId
        log.info("Wallet Id: {}\n and username: {}", loanApplication.getWalletId(), userCredentialRepository.findByWalletId(walletId).get().getEmail());
        LoanCalculatorResponse loanCalculatorResponse = loanCalculatorService.calculateLoan(LoanCalculatorRequest.builder()
                        .loanAmount(request.getAmountDisbursed())
                        .loanType(loanApplication.getLoanType())
                        .loanTenor(loanApplication.getLoanTenor())
                .build()).getBody().getLoanCalculatorResponse();

        double amountToPayBack = loanCalculatorResponse.getAmountToPayBack();
        log.info("Amount to pay back: {}", amountToPayBack);
        LoanDisbursal loanDisbursal = LoanDisbursal.builder()
                .loanRefNo(request.getLoanRefNo())
                .walletId(walletId)
                .appliedAmount(loanApplication.getLoanAmount())
                .amountDisbursed(request.getAmountDisbursed())
                .dateDisbursed(LocalDateTime.now())
                .disbursedBy(email)
                .amountToPayBack(amountToPayBack)
                .build();
        LoanDisbursal disbursal = loanDisbursalRepository.save(loanDisbursal);
        loanApplication.setLoanApplicationStatus("DISBURSED");
        loanApplicationRepository.save(loanApplication);

        //update wallet balance
        UserCredential userCredential = userCredentialRepository.findByWalletId(walletId).get();
        userCredential.setAccountBalance(disbursal.getAmountDisbursed());
        userCredentialRepository.save(userCredential);

        //update Loan application status, disbursed,
        //update Repayments



        Repayments repayments = Repayments.builder()
                .loanTenor(loanApplication.getLoanTenor())
                .nextRepaymentDate(disbursal.getDateDisbursed().plusDays(loanApplication.getLoanTenor()))
                .amountPaid(0)
                .repaymentStatus("PENDING")
                .loanRefNo(request.getLoanRefNo())
                .build();
        repaymentsRepository.save(repayments);

        transactionService.saveTransaction(Transactions.builder()
                        .walletId(walletId)
                        .transactionType("LOAN DISBURSAL")
                        .transactionStatus("COMPLETED")
                        .transactionAmount(disbursal.getAmountDisbursed())
                        .transactionRef(generateTxnRef("CREDIT"))
                        .createdAt(LocalDateTime.now())
                .build());
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(SUCCESS_MESSAGE)
                        .disbursalRequest(modelMapper.map(disbursal, DisbursalRequest.class))
                .build());
    }

}
