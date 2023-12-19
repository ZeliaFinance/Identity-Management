package com.zeliafinance.identitymanagement.banks.controller;

import com.zeliafinance.identitymanagement.banks.dto.BankRequest;
import com.zeliafinance.identitymanagement.banks.dto.NameEnquiryRequest;
import com.zeliafinance.identitymanagement.banks.service.BankService;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/banks")
@AllArgsConstructor
public class BankController {
    private BankService bankService;

    @PostMapping
    public ResponseEntity<CustomResponse> saveBank(){
        return bankService.saveBanksFromBani();
    }

    @GetMapping
    public ResponseEntity<CustomResponse> fetchBanks(@RequestParam (value = "provider", defaultValue = "PROVIDUS") String provider){
        return bankService.fetchBanks(provider);
    }

    @PostMapping("saveSingleBank")
    public ResponseEntity<CustomResponse> saveSingleBank(@RequestBody BankRequest request){
        return bankService.saveBanks(request);
    }

    @PostMapping("nameEnquiry")
    public ResponseEntity<CustomResponse> nameEnquiry(@RequestBody NameEnquiryRequest nameEnquiryRequest){
        return bankService.verifyAccount(nameEnquiryRequest);
    }
}
