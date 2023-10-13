package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.request.ChargeCardRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.response.ChargeCardResponse;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlutterwaveService {

    @Autowired
    AccountUtils accountUtils;

    @Value("${flutterwave.publicKey}")
    private String publicKey;
    @Value("${flutterwave.privateKey}")
    private String privateKey;
    @Value("${flutterwave.encryptionKey}")
    private String encryptionKey;
    @Value("${flutterwave.baseUrl}")
    private String baseUrl;

    public ChargeCardResponse chargeCard(ChargeCardRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/v3/charges?type=card";
        log.info("full url: {}", url);
        HttpEntity<ChargeCardRequest> entity = new HttpEntity<>(request, headers());
        ResponseEntity<ChargeCardResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ChargeCardResponse.class);
        return response.getBody();

    }

    private HttpHeaders headers(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", accountUtils.decode(privateKey, 3));
        httpHeaders.set("Content-Type", "application/json");
        return httpHeaders;
    }
}
