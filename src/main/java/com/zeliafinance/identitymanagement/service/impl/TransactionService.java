package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.entity.Transactions;
import com.zeliafinance.identitymanagement.repository.TransactionRepository;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TransactionService {

    private TransactionRepository transactionRepository;

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
}
