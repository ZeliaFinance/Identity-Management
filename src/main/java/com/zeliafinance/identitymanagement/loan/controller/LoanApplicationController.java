package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/loanApplication")
public class LoanApplicationController {

    private LoanApplicationService service;

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
    public ResponseEntity<CustomResponse> fetchAllLoans(@RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "50") int pageSize){
        return service.fetchAllLoanApplications(pageNo, pageSize);
    }

    //Logged-in user
    @GetMapping("loanApplicationHistory")
    public ResponseEntity<CustomResponse> loanHistory(@RequestParam(defaultValue = "1") int pageNo,
                                                      @RequestParam(defaultValue = "50") int pageSize){
        return service.loanApplicationHistory(pageNo, pageSize);
    }


    @GetMapping("loanApplicationsByStatus")
    public ResponseEntity<CustomResponse> loanApplicationByStatus(@RequestParam String loanApplicationStatus,
                                                                  @RequestParam(defaultValue = "1") int pageNo,
                                                                  @RequestParam(defaultValue = "50") int pageSize){
        return service.viewLoanApplicationsByStatus(loanApplicationStatus, pageNo, pageSize);
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

    @GetMapping("fetchLoanByRefNo")
    public ResponseEntity<CustomResponse> fetchSingleLoan(@RequestParam String loanRefNo){
        return service.fetchByLoanRefNo(loanRefNo);
    }

    @PostMapping("/approveLoan")
    @PreAuthorize("{hasRole('ROLE_SUPER_ADMIN')}")
    public ResponseEntity<CustomResponse> approveLoan(@RequestParam String loanRefNo){
        return service.approveLoan(loanRefNo);
    }

    @PostMapping("/denyLoan")
    @PreAuthorize("{hasRole('ROLE_SUPER_ADMIN')}")
    public ResponseEntity<CustomResponse> denyLoan(@RequestParam String loanRefNo){
        return service.denyLoan(loanRefNo);
    }
}
