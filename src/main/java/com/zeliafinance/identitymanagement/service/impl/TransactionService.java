package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.EmailDetails;
import com.zeliafinance.identitymanagement.dto.TransferRequest;
import com.zeliafinance.identitymanagement.dto.WalletDetailsDto;
import com.zeliafinance.identitymanagement.entity.Transactions;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.TransactionRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TransactionService {

    private TransactionRepository transactionRepository;
    private UserCredentialRepository userCredentialRepository;
    private EmailService emailService;
    private AccountUtils accountUtils;

    public ResponseEntity<CustomResponse> saveTransaction(Transactions transactions){
        Transactions newTransaction = Transactions.builder()
                .transactionRef(transactions.getTransactionRef())
                .transactionType(transactions.getTransactionType())
                .transactionStatus(transactions.getTransactionStatus())
                .transactionAmount(transactions.getTransactionAmount())
                .createdAt(LocalDateTime.now())
                .walletId(transactions.getWalletId())
                .externalRefNumber(transactions.getExternalRefNumber())
                .build();

        Transactions savedTransaction = transactionRepository.save(transactions);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(savedTransaction)
                .build());
    }

    public ResponseEntity<CustomResponse> accountEnquiry(String walletId){
        Optional<UserCredential> userCredential = userCredentialRepository.findByWalletId(walletId);
        if (userCredential.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("User with wallet Id not found")
                    .build());
        }
        WalletDetailsDto walletDetailsDto = WalletDetailsDto.builder()
                .accountName(userCredential.get().getFirstName() + " " + userCredential.get().getLastName() + " " + userCredential.get().getOtherName())
                .walletId(walletId)
                .accountBalance(userCredential.get().getAccountBalance())
                .build();

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(walletDetailsDto)
                .build());

    }

    public ResponseEntity<CustomResponse> transactionHistory(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();
        List<Transactions> transactionsList = transactionRepository.findAll().stream()
                .filter(transactions -> transactions.getWalletId().equals(walletId))
                .sorted(Comparator.comparing(Transactions::getCreatedAt).reversed())
                .toList();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(transactionsList)
                .build());
    }

    public ResponseEntity<CustomResponse> walletToWalletTransfer(TransferRequest transferRequest){
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(loggedInUserEmail).get();
        //check for amount adequacy
        if (userCredential.getAccountBalance() < transferRequest.getAmount()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.INSUFFICIENT_BALANCE)
                    .build());
        }
        Optional<UserCredential> beneficiary = userCredentialRepository.findByWalletId(transferRequest.getBeneficiaryAccountNumber());
        if (beneficiary.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("User with Wallet Id does not exist")
                    .build());
        }
        userCredential.setAccountBalance(userCredential.getAccountBalance()- transferRequest.getAmount());
        beneficiary.get().setAccountBalance(beneficiary.get().getAccountBalance() + transferRequest.getAmount());
        userCredentialRepository.save(userCredential);
        userCredentialRepository.save(beneficiary.get());
        emailService.sendEmailAlert(EmailDetails.builder()
                        .subject("DEBIT ALERT")
                        .recipient(loggedInUserEmail)
                        .messageBody("You successfully transferred " + transferRequest.getAmount() + " to " + beneficiary.get().getFirstName() + " " + beneficiary.get().getLastName())
                .build());

        emailService.sendEmailAlert(EmailDetails.builder()
                        .subject("CREDIT")
                        .recipient(beneficiary.get().getEmail())
                        .messageBody("You received a sum of ₦" + transferRequest.getAmount() + " from " + userCredential.getFirstName() + " " + userCredential.getLastName() + " " + userCredential.getOtherName())
                .build());
        transactionRepository.save(Transactions.builder()
                .transactionType("Debit")
                .transactionStatus("COMPLETED")
                .transactionAmount(transferRequest.getAmount())
                .transactionRef(AccountUtils.generateTxnRef("DEBIT"))
                .createdAt(LocalDateTime.now())
                .walletId(userCredential.getWalletId())
                .build());

        transactionRepository.save(Transactions.builder()
                        .walletId(beneficiary.get().getWalletId())
                        .transactionType("CREDIT")
                        .createdAt(LocalDateTime.now())
                        .transactionRef(AccountUtils.generateTxnRef("CREDIT"))
                        .transactionAmount(transferRequest.getAmount())
                        .transactionStatus("COMPLETED")
                .build());

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(transferRequest)
                .build());
    }

//    public ResponseEntity<CustomResponse> transfer(){
//
//    }
}
