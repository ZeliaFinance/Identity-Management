package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanRoutingService;
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

    private LoanRoutingService loanRoutingService;

    @PostMapping
    public ResponseEntity<CustomResponse> applyForLoan(@RequestBody LoanApplicationRequest request) throws Exception {
        return loanRoutingService.processLoanApplication(request.getLoanType(), request);
    }

}
