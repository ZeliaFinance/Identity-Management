package com.zeliafinance.identitymanagement.thirdpartyapis.providus.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request.CreateDynamicAccountRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request.CreateReservedAccountRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.CreateDynamicAccountResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.CreateReservedAccountResponse;
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
public class ProvidusService {

    @Value("${providus.baseUrl}")
    private String baseUrl;
    @Value("${providus.clientId}")
    private String clientId;
    @Value("${providus.clientSecret}")
    private String clientSecret;
    @Value("${providus.x-auth-sign}")
    private String xAuthSign;
    @Autowired
    private AccountUtils accountUtils;

    public CreateDynamicAccountResponse createDynamicAccount(CreateDynamicAccountRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "PiPCreateDynamicAccountNumber";
        log.info("full url: {}", url);
        HttpEntity<CreateDynamicAccountRequest> entity = new HttpEntity<>(request, headers());
        ResponseEntity<CreateDynamicAccountResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, CreateDynamicAccountResponse.class);
        return responseEntity.getBody();
    }

    public CreateReservedAccountResponse createReservedAccount(CreateReservedAccountRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "PiPCreateReservedAccountNumber";
        log.info("full url: {}", url);
        HttpEntity<CreateReservedAccountRequest> entity = new HttpEntity<>(request, headers());
        ResponseEntity<CreateReservedAccountResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, CreateReservedAccountResponse.class);
        return responseEntity.getBody();
    }

    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Auth-Signature", accountUtils.decode(xAuthSign, 3));
        headers.set("Client-Id", accountUtils.decode(clientId, 3));

        return headers;
    }
}
