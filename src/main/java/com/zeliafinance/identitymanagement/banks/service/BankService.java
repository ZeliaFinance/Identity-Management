package com.zeliafinance.identitymanagement.banks.service;

import com.zeliafinance.identitymanagement.banks.entity.Bank;
import com.zeliafinance.identitymanagement.banks.repository.BankRepository;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.Info;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.GetBanksResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.service.ProvidusService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class BankService {
    private BankRepository bankRepository;
    private ProvidusService providusService;

    public ResponseEntity<CustomResponse> saveBanksFromProvidus(){
        GetBanksResponse getBanksResponse = providusService.getBanks();
        List<Bank> savedBanks = new ArrayList<>();

        getBanksResponse.getBanks().forEach(bankInfo -> {
            Bank bank = new Bank();
            bank.setProvider("PROVIDUS");
            bank.setBankCode(bankInfo.getBankCode());
            bank.setBankName(bankInfo.getBankName());
            bank.setCreatedAt(LocalDateTime.now());
            bank.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
            savedBanks.add(bankRepository.save(bank));
        });

        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(200)
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .responseBody(savedBanks)
                .build());
    }

    public ResponseEntity<CustomResponse> fetchBanks(String provider){
        List<Bank> bankList = bankRepository.findAll().stream().filter(bank -> bank.getProvider().equalsIgnoreCase(provider)).toList();
        return ResponseEntity.ok(CustomResponse.builder()
                .info(Info.builder()
                        .totalElements((long)bankList.size())
                        .build())
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(bankList)

                .build());
    }
}
