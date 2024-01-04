package com.zeliafinance.identitymanagement.loanDisbursal.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loanDisbursal.dto.DisbursalRequest;
import com.zeliafinance.identitymanagement.loanDisbursal.service.LoanDisbursalService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/loanDisbursal")
@AllArgsConstructor
public class LoanDisbursalController {

    private LoanDisbursalService loanDisbursalService;

    @PostMapping
    @PreAuthorize("{hasRole('ROLE_SUPER_ADMIN')}")
    public ResponseEntity<CustomResponse> disburseLoan(@RequestBody DisbursalRequest disbursalRequest) throws MessagingException, TemplateException, IOException {
        return loanDisbursalService.disburseLoan(disbursalRequest);
    }
}
