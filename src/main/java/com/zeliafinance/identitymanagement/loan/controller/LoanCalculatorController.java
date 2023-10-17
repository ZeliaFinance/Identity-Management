package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanCalculatorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/loanCalculator")
public class LoanCalculatorController {

    private LoanCalculatorService service;

    @PostMapping()
    public ResponseEntity<CustomResponse> calculateLoan(@RequestBody LoanCalculatorRequest request){
        return service.calculateLoan(request);
    }

}
