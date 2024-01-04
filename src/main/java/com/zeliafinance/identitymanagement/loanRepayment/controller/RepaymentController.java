package com.zeliafinance.identitymanagement.loanRepayment.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loanRepayment.dto.PayOffLoanRequest;
import com.zeliafinance.identitymanagement.loanRepayment.service.RepaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;

    @GetMapping("/userRepaymentHistory")
    public ResponseEntity<CustomResponse> getUserRepaymentHistory(){
        return repaymentService.userRepaymentHistory();
    }

    @PostMapping("payOffLoan")
    public ResponseEntity<CustomResponse> payOffLoan(@RequestBody PayOffLoanRequest payOffLoanRequest){
        return repaymentService.payOffLoan(payOffLoanRequest);
    }

    @PostMapping("payOffInstalment")
    public ResponseEntity<CustomResponse> payOffInstalment(@RequestBody PayOffLoanRequest payOffLoanRequest){
        return repaymentService.payOffLoanInstallments(payOffLoanRequest);
    }
}
