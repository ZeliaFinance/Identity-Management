package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

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
    public ResponseEntity<CustomResponse> stageTwo(
                                                   @RequestParam(value = "loanRefNo") String loanRefNo,
                                                   @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageTwo(loanRefNo, request);
    }

    @PostMapping("stageThree")
    public ResponseEntity<CustomResponse> stageThree(@RequestParam(value = "file1", required = false) String file1,
                                                     @RequestParam(value = "file2", required = false) String file2,
                                                     @RequestParam(value = "loanRefNo") String loanRefNo,
                                                     @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageThree(file1, file2, loanRefNo, request);
    }

    @PostMapping("stageFour")
    public ResponseEntity<CustomResponse> stageFour(@RequestPart(value = "file1") final Optional<MultipartFile> file1,
                                                    @RequestPart(value = "file2") final Optional<MultipartFile> file2,
                                                    @RequestParam(value = "loanRefNo") String loanRefNo,
                                                    @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageFour(file1, file2, loanRefNo, request);
    }

    @PostMapping("stageFive")
    public ResponseEntity<CustomResponse> stageFive(@RequestParam(value = "loanRefNo") String loanRefNo,
                                                    @RequestBody LoanApplicationRequest request) throws Exception {
        return service.stageFive(loanRefNo, request);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("loanApplicationList")
    public ResponseEntity<CustomResponse> fetchAllLoans(){
        return service.fetchAllLoanApplications();
    }

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
}
