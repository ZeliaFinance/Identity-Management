package com.zeliafinance.identitymanagement.thirdpartyapis.bani.controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request.CreateCustomerDto;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response.CreateCustomerResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.service.BaniService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/bani")
public class BaniController {
    private BaniService baniService;

    @PostMapping("/createCustomer")
    public CreateCustomerResponse createCustomer(@Valid @RequestBody CreateCustomerDto createCustomerDto){
        return baniService.createCustomer(createCustomerDto);
    }
}
