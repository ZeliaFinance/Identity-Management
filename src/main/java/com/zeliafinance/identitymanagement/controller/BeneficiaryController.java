package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.service.impl.BeneficiaryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/beneficiary")
@AllArgsConstructor
public class BeneficiaryController {

    private BeneficiaryService beneficiaryService;

    @GetMapping
    public ResponseEntity<CustomResponse> fetchBeneficiaries(){
        return beneficiaryService.fetchBeneficiary();
    }
}
