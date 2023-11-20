package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/loanApplication")
public class LoanApplicationController {

    private final LoanApplicationService service;

    @PostMapping("stageOne")
    public ResponseEntity<CustomResponse> stageOne(@RequestBody LoanApplicationRequest request){
        return service.stageOne(request);
    }

    @PostMapping(value = "stageTwo")
    public ResponseEntity<CustomResponse> stageTwo(@RequestParam(value = "loanRefNo") String loanRefNo,
                                                   @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageTwo(loanRefNo, request);
    }

    @PostMapping("stageThree")
    public ResponseEntity<CustomResponse> stageThree(@RequestParam(value = "loanRefNo") String loanRefNo,
                                                     @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageThree(loanRefNo, request);
    }

    @PostMapping("stageFour")
    public ResponseEntity<CustomResponse> stageFour(@RequestParam(value = "loanRefNo") String loanRefNo,
                                                    @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageFour(loanRefNo, request);
    }

    @PostMapping("stageFive")
    public ResponseEntity<CustomResponse> stageFive(@RequestParam(value = "loanRefNo") String loanRefNo,
                                                    @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageFive(loanRefNo, request);
    }

    @GetMapping("loanApplicationList")
    public ResponseEntity<CustomResponse> fetchAllLoans(){
        return service.fetchAllLoanApplications();
    }

    //Logged-in user
    @GetMapping("loanApplicationHistory")
    public ResponseEntity<CustomResponse> loanHistory(){
        return service.loanApplicationHistory();
    }

    @PostMapping("updateStageOne")
    public ResponseEntity<CustomResponse> updateStageOne(@RequestParam String loanRefNo, @RequestBody LoanApplicationRequest request) throws Exception {
        return service.updateStageOne(loanRefNo, request);
    }

    @GetMapping("searchByPhoneNumber")
    public ResponseEntity<CustomResponse> searchByPhoneNumber(@RequestParam String phoneNumber){
        return service.searchByPhoneNumber(phoneNumber);
    }

    @GetMapping("searchByLoanApplicationStatus")
    public ResponseEntity<CustomResponse> searchByLoanAppStatus(@RequestParam String loanApplicationStatus){
        return service.searchByLoanAppStatus(loanApplicationStatus);
    }

    @DeleteMapping("deleteLoan/{loanId}")
    public ResponseEntity<CustomResponse> deleteLoan(@PathVariable long loanId){
        return service.deleteLoan(loanId);
    }

    @PostMapping("cancelLoanApplication")
    public ResponseEntity<CustomResponse> cancelLoanApplication(@RequestParam String loanRefNo){
        return service.cancelLoan(loanRefNo);
    }
}
