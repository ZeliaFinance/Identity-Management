package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("stageTwo")
    public ResponseEntity<CustomResponse> stageTwo(@RequestPart(value = "file") final Optional<MultipartFile> multipartFile,
                                                   @RequestParam(value = "loanRefNo") String loanRefNo,
                                                   @RequestPart LoanApplicationRequest request) throws Exception {
        return service.stageTwo(multipartFile, loanRefNo, request);
    }

}
