package com.zeliafinance.identitymanagement.otp.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.otp.dto.OtpRequest;
import com.zeliafinance.identitymanagement.otp.dto.OtpValidationRequest;
import com.zeliafinance.identitymanagement.otp.service.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/otp")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("sendOtp")
    public void sendOtp(@RequestBody OtpRequest otpRequest){
        otpService.sendOtp(otpRequest);
    }

    @PostMapping("validateOtp")
    public ResponseEntity<CustomResponse> validateOtp(@RequestBody OtpValidationRequest request){
        return otpService.validateOtp(request);
    }
}
