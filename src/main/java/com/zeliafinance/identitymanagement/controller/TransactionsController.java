package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.AuthorizeCardRequest;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.TransferRequest;
import com.zeliafinance.identitymanagement.service.impl.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionsController {

    private final TransactionService transactionService;

    @GetMapping("walletDetails")
    public ResponseEntity<CustomResponse> accountDetailsEnquiry(@RequestParam String walletId){
        return transactionService.accountEnquiry(walletId);
    }

    @GetMapping("transactionHistory")
    public ResponseEntity<CustomResponse> transactionHistory(){
        return transactionService.transactionHistory();
    }

    @PostMapping("walletToWalletTransfer")
    public ResponseEntity<CustomResponse> walletToWalletTransfer(@RequestBody TransferRequest transferRequest){
        return transactionService.walletToWalletTransfer(transferRequest);
    }

    @PostMapping("walletToCommercialBankTransfer")
    public ResponseEntity<CustomResponse> walletToCommercialBankTransfer(@RequestBody TransferRequest transferRequest){
        return transactionService.walletToCommercialBankTransfer(transferRequest);
    }

    @PostMapping("cardAuthorization")
    public ResponseEntity<CustomResponse> cardAuthorization(@RequestBody AuthorizeCardRequest authorizeCardRequest){
        return transactionService.authorizeCard(authorizeCardRequest);
    }
}
