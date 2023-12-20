package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.dto.BeneficiaryRequest;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.entity.Beneficiary;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.BeneficiaryRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BeneficiaryService {
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserCredentialRepository userCredentialRepository;

    public ResponseEntity<CustomResponse> saveBeneficiary(BeneficiaryRequest beneficiaryRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        List<Beneficiary> existingBeneficiary = beneficiaryRepository.findAll().stream().filter(beneficiary -> beneficiary.getBeneficiaryAccountNumber().equals(beneficiaryRequest.getBeneficiaryAccountNumber()) && beneficiary.getBeneficiaryBank().equalsIgnoreCase(beneficiaryRequest.getBeneficiaryBank())).toList();
        if (!existingBeneficiary.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Beneficiary already exists")
                    .build());
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .beneficiaryBank(beneficiaryRequest.getBeneficiaryBank())
                .beneficiaryAccountNumber(beneficiaryRequest.getBeneficiaryAccountNumber())
                .walletId(userCredential.getWalletId())
                .beneficiaryName(beneficiaryRequest.getAccountName())
                .build();
        beneficiaryRepository.save(beneficiary);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> fetchBeneficiary(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();

        List<Beneficiary> beneficiaryList = beneficiaryRepository.findAll().stream().filter(beneficiary -> beneficiary.getWalletId().equals(userCredential.getWalletId())).toList();

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(beneficiaryList)
                .build());
    }
}
