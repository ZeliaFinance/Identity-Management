package com.zeliafinance.identitymanagement.debitmandate.controller;

import com.zeliafinance.identitymanagement.debitmandate.dto.CardRequest;
import com.zeliafinance.identitymanagement.debitmandate.dto.InstitutionRequest;
import com.zeliafinance.identitymanagement.debitmandate.service.DebitMandateService;
import com.zeliafinance.identitymanagement.debitmandate.service.InstitutionService;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/debitMandate")
public class InstitutionController {
    private final InstitutionService institutionService;
    private final DebitMandateService debitMandateService;

    @PostMapping
    public ResponseEntity<CustomResponse> saveInstitution(@RequestParam String loanRefNo, @RequestBody InstitutionRequest institutionRequest){
        return institutionService.saveInstitution(loanRefNo, institutionRequest);
    }

    @PostMapping("/addCard")
    public ResponseEntity<CustomResponse> addCard(@RequestBody CardRequest cardRequest){
        return debitMandateService.addCard(cardRequest);
    }
}
