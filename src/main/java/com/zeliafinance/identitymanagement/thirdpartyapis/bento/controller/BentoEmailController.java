package com.zeliafinance.identitymanagement.thirdpartyapis.bento.controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.bento.dto.EmailRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.bento.dto.EmailResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.bento.service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/bento-email")
@AllArgsConstructor
public class BentoEmailController {
    private EmailService emailService;


    @PostMapping
    public EmailResponse sendEmail(@RequestBody EmailRequest request){
        return emailService.sendEmail(request);
    }
}
