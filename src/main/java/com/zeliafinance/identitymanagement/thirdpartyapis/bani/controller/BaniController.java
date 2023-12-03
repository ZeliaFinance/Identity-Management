package com.zeliafinance.identitymanagement.thirdpartyapis.bani.controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.service.BaniService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/bani")
public class BaniController {
    private BaniService baniService;

    @PostMapping("/createCustomer")
    public CreateCustomerResponse createCustomer(@Valid @RequestBody CreateCustomerDto createCustomerDto){
        return baniService.createCustomer(createCustomerDto);
    }

    @PostMapping("/createVirtualAccount")
    public CreateVirtualAccountResponse createVirtualAccount(@RequestBody CreateVirtualAccountRequest request){
        return baniService.createVirtualAccount(request);
    }

    @PostMapping("/webHook")
    public String webHook(@RequestBody WebHookRequest request){
        return baniService.webHookResponse(request);
    }

    @GetMapping("/banks")
    public BanksResponse getBanks(){
        return baniService.fetchAllBanks();
    }

    @PostMapping("/payout")
    public PayoutResponse payout(@RequestBody PayoutRequest payoutRequest){
        return baniService.payout(payoutRequest);
    }

    @GetMapping("/verifyAccount")
    public VerifyAccountResponse verifyAccountResponse(@RequestBody VerifyAccountRequest verifyAccountRequest){
        return baniService.verifyAccount(verifyAccountRequest);
    }
}
