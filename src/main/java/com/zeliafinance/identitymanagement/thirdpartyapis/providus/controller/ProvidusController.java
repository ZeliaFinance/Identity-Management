package com.zeliafinance.identitymanagement.thirdpartyapis.providus.controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request.CreateDynamicAccountRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request.CreateReservedAccountRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request.FundTransferRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request.GetNipAccountRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.CreateDynamicAccountResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.CreateReservedAccountResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.FundTransferResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.GetNipAccountResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.service.ProvidusService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/providus")
@AllArgsConstructor
public class ProvidusController {

    private ProvidusService service;

    @PostMapping("createDynamicAccount")
    public CreateDynamicAccountResponse createDynamicAccount(@RequestBody CreateDynamicAccountRequest request){
        return service.createDynamicAccount(request);
    }

    @PostMapping("createReservedAccount")
    public CreateReservedAccountResponse createReservedAccount(@RequestBody CreateReservedAccountRequest request){
        return service.createReservedAccount(request);
    }

    @PostMapping("getNipAccount")
    public GetNipAccountResponse getNipAccountResponse(@RequestBody GetNipAccountRequest request){
        return service.getNipAccount(request);
    }

    @PostMapping("fundTransfer")
    public FundTransferResponse doFundTransfer(@RequestBody FundTransferRequest request){
        return service.doFundTransfer(request);
    }
}
