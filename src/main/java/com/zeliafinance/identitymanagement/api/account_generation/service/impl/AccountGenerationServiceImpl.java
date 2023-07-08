package com.zeliafinance.identitymanagement.api.account_generation.service.impl;

import com.zeliafinance.identitymanagement.api.account_generation.dto.request.AccountGenerationRequest;
import com.zeliafinance.identitymanagement.api.account_generation.dto.response.AccountGenerationResponse;
import com.zeliafinance.identitymanagement.api.account_generation.service.AccountGenerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;




@Service
public class AccountGenerationServiceImpl implements AccountGenerationService {

    private final RestTemplate restTemplate;
    @Value("${zelia.base.url}")
    private final String baseUrl;

    @Value("${account.generation.path}")
    private final String accountGenerationPath;

    @Value("${x.auth.signature}")
    private final String authSignature;

    @Value("${client.id}")
    private final String clientId;

    public AccountGenerationServiceImpl(RestTemplate restTemplate, String baseUrl, String accountGenerationPath, String authSignature, String clientId) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.accountGenerationPath = accountGenerationPath;
        this.authSignature = authSignature;
        this.clientId = clientId;
    }

    @Override
    public AccountGenerationResponse createAccount(AccountGenerationRequest accountGenerationRequest) {
        try {
            String url = baseUrl + accountGenerationPath;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Client-Id", clientId);
            headers.set("X-Auth-Signature", authSignature);

            HttpEntity<AccountGenerationRequest> entity = new HttpEntity<>(accountGenerationRequest, headers);

          AccountGenerationResponse response = restTemplate.exchange(url, HttpMethod.POST, entity, AccountGenerationResponse.class).getBody();
          return response;
        } catch(Exception exception) {
            throw new RuntimeException("Unable to generate account");
        }

    }
}
