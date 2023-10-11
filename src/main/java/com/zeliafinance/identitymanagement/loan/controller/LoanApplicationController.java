package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/loanApplication")
public class LoanApplicationController {

    private LoanApplicationService service;

    @PostMapping("loanCalculator")
    public ResponseEntity<CustomResponse> calculateLoan(@RequestBody LoanApplicationRequest request){
        return service.calculateLoan(request);
    }

    @PostMapping
    public ResponseEntity<CustomResponse> applyForLoan(@RequestBody LoanApplicationRequest request){
        return service.applyForLoan(request);
    }
}
