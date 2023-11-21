package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanOfferingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/loanOffering")
public class LoanOfferingController {

    private LoanOfferingService loanOfferingService;

    @PostMapping("saveOffering")
    public ResponseEntity<CustomResponse> saveLoanOffering(@RequestBody LoanOfferingRequest request){
        return loanOfferingService.saveLoanOffering(request);
    }

    @GetMapping("fetchAllOfferings")
    public ResponseEntity<CustomResponse> fetchAllLoanOfferings(){
        return loanOfferingService.fetchAllLoanOfferings();
    }

    @GetMapping("fetchOfferingByLoanProduct")
    public ResponseEntity<CustomResponse> fetchOfferingByLoanProduct(@RequestParam String loanProduct){
        return loanOfferingService.fetchLoanOfferingByProductName(loanProduct);
    }
}
