package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.banks.entity.Bank;
import com.zeliafinance.identitymanagement.banks.repository.BankRepository;
import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.Transactions;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.TransactionRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request.PayoutRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response.PayoutResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.service.BaniService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionService {

    private TransactionRepository transactionRepository;
    private UserCredentialRepository userCredentialRepository;
    private EmailService emailService;
    private ModelMapper modelMapper;
    private BaniService baniService;
    private BankRepository bankRepository;
    private BeneficiaryService beneficiaryService;

    public void saveTransaction(Transactions transactions){
        Transactions newTransaction = Transactions.builder()
                .transactionRef(transactions.getTransactionRef())
                .transactionType(transactions.getTransactionType())
                .transactionStatus(transactions.getTransactionStatus())
                .transactionAmount(transactions.getTransactionAmount())
                .createdAt(LocalDateTime.now())
                .walletId(transactions.getWalletId())
                .externalRefNumber(transactions.getExternalRefNumber())
                .transactionCategory(transactions.getTransactionCategory())
                .build();

        Transactions savedTransaction = transactionRepository.save(newTransaction);

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
        List<TransactionsResponse> transactionsList = transactionRepository.findAll().stream()
                .filter(transactions -> transactions.getWalletId().equals(walletId))
                .sorted(Comparator.comparing(Transactions::getCreatedAt).reversed())
                .map(transactions -> modelMapper.map(transactions, TransactionsResponse.class))
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

    public ResponseEntity<CustomResponse> walletToCommercialBankTransfer(TransferRequest transferRequest){
        Bank bank = bankRepository.findByBankName(transferRequest.getBankName());
        String transactionRef = AccountUtils.generateTxnRef("DEBIT");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String walletId = userCredentialRepository.findByEmail(email).get().getWalletId();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        if (userCredential.getAccountBalance() < transferRequest.getAmount()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("INSUFFICIENT BALANCE")
                    .build());
        }
        log.info("Wallet Id: {}", walletId);
        log.info("Transaction Ref: {}", transactionRef);
        log.info("Zelia Request: {}", transferRequest);
        log.info("List code: {}", bank.getBankCode());
        PayoutRequest payoutRequest = PayoutRequest.builder()
                .payout_step("direct")
                .receiver_currency("NGN")
                .receiver_amount(String.valueOf(transferRequest.getAmount()))
                .transfer_method("bank")
                .transfer_receiver_type("personal")
                .receiver_account_num(transferRequest.getBeneficiaryAccountNumber())
                .receiver_country_code("NG")
                .receiver_sort_code(bank.getBankCode())
                .receiver_account_name(transferRequest.getAccountName())
                .sender_amount(String.valueOf(transferRequest.getAmount()))
                .sender_currency("NGN")
                .transfer_note("Wallet to Bank Transfer")
                .transfer_ext_ref(transactionRef)
                .build();
        log.info("Bani Request: {}", payoutRequest);
        PayoutResponse payoutResponse = baniService.payout(payoutRequest);
        log.info("Response: {}", payoutResponse);

        if(!payoutResponse.isStatus()){
            return ResponseEntity.internalServerError().body(CustomResponse.builder()
                            .statusCode(500)
                            .responseMessage("Transfer failed")
                    .build());
        }
        else {
            if (transferRequest.isSaveBeneficiary()){
                beneficiaryService.saveBeneficiary(BeneficiaryRequest.builder()
                                .walletId(walletId)
                                .beneficiaryBank(transferRequest.getBankName())
                                .beneficiaryAccountNumber(transferRequest.getBeneficiaryAccountNumber())
                                .accountName(transferRequest.getAccountName())
                        .build());
            }
            //update transactions, update account balance
            Transactions transactions = Transactions.builder()
                    .walletId(walletId)
                    .transactionStatus("COMPLETED")
                    .transactionRef(transactionRef)
                    .transactionType("Wallet to Bank Transfer")
                    .transactionAmount(transferRequest.getAmount())
                    .transactionCategory("DEBIT")
                    .externalRefNumber(null)
                    .build();
            userCredential.setAccountBalance(userCredential.getAccountBalance()-transferRequest.getAmount());
            transactionRepository.save(transactions);
            userCredentialRepository.save(userCredential);

            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage("SUCCESS")
                            .responseBody(transferRequest)
                    .build());

        }

    }
}
