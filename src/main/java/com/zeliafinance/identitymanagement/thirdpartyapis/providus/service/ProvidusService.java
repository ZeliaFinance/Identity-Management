package com.zeliafinance.identitymanagement.thirdpartyapis.providus.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response.*;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
    @Value("${providus.transactionBaseUrl}")
    private String transactionBaseUrl;
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

    public GetNipAccountResponse getNipAccount(GetNipAccountRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = transactionBaseUrl + "/GetNIPAccount";
        log.info("full url: {}", url);
        HttpEntity<GetNipAccountRequest> entity = new HttpEntity<>(request, contentHeader());
        ResponseEntity<GetNipAccountResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, GetNipAccountResponse.class);
        log.info("Response: {}", responseEntity);
        return responseEntity.getBody();
    }

    public FundTransferResponse doFundTransfer(FundTransferRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = transactionBaseUrl + "/NIPFundTransfer";
        log.info("full url: {}", url);
        HttpEntity<FundTransferRequest> entity = new HttpEntity<>(request, contentHeader());
        ResponseEntity<FundTransferResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, FundTransferResponse.class);
        return responseEntity.getBody();
    }

    public BalanceEnquiryResponse doBalanceEnquiry(BalanceEnquiryRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = transactionBaseUrl + "/GetProvidusAccount";
        log.info("full url: {}", url);
        HttpEntity<BalanceEnquiryRequest> entity = new HttpEntity<>(request, contentHeader());
        ResponseEntity<BalanceEnquiryResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, BalanceEnquiryResponse.class);
        return responseEntity.getBody();
    }

    public GetBanksResponse getBanks(){
        RestTemplate restTemplate = new RestTemplate();
        String url = transactionBaseUrl + "/GetNIPBanks";
        log.info("full url: {}", url);
        HttpEntity entity = new HttpEntity<>(contentHeader());
        ResponseEntity<GetBanksResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, GetBanksResponse.class);
        return responseEntity.getBody();
    }

    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Auth-Signature", accountUtils.decode(xAuthSign, 3));
        headers.set("Client-Id", accountUtils.decode(clientId, 3));

        return headers;
    }

    private HttpHeaders contentHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypeList);

        return headers;
    }
}
