package com.zeliafinance.identitymanagement.thirdpartyapis.termii.controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.termii.dto.request.SmsDto;
import com.zeliafinance.identitymanagement.thirdpartyapis.termii.dto.response.SmsResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.termii.service.SmsImpl;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/termii/sendSms")
@AllArgsConstructor
public class SmsController {

    private SmsImpl smsService;

    @PostMapping
    public SmsResponse sendSms(@RequestBody SmsDto smsDto){
        return smsService.sendSms(smsDto);
    }
}
