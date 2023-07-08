package com.zeliafinance.identitymanagement.api.account_generation.controller;

import com.zeliafinance.identitymanagement.api.account_generation.dto.request.AccountGenerationRequest;
import com.zeliafinance.identitymanagement.api.account_generation.dto.response.AccountGenerationResponse;
import com.zeliafinance.identitymanagement.api.account_generation.service.AccountGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/zelia")
public class AccountGenerationController {
    private final AccountGenerationService accountGenerationService;

    public AccountGenerationController(AccountGenerationService accountGenerationService) {
        this.accountGenerationService = accountGenerationService;
    }

    @PostMapping("/generate-account")
    public ResponseEntity<Object> generateAccount(@RequestBody AccountGenerationRequest accountGenerationRequest) {
        return ResponseEntity.ok(accountGenerationService.createAccount(accountGenerationRequest));
    }
}
