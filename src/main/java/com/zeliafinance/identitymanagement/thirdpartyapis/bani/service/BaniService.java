package com.zeliafinance.identitymanagement.thirdpartyapis.bani.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.utils.SignatureGenerator;
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
@RequiredArgsConstructor
@Slf4j
public class BaniService {

    @Value("${bani.baseUrl}")
    private String baseUrl;

    @Value("${bani.accessToken}")
    private String accessToken;

    @Value("${bani.moniSignature}")
    private String moniSignature;
    @Value("${bani.privateKey}")
    private String privateKey;

    @Autowired
    private AccountUtils accountUtils;

    public CreateCustomerResponse createCustomer(CreateCustomerDto createCustomerDto){
        log.info("dto: {}", createCustomerDto);
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/comhub/add_my_customer";
        log.info("full url: {}", url);
        HttpEntity<CreateCustomerDto>  entity = new HttpEntity<>(createCustomerDto, headers());
        log.info("Entity: {}", entity);
        ResponseEntity<CreateCustomerResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, CreateCustomerResponse.class);
        return responseEntity.getBody();
    }

    public CreateVirtualAccountResponse createVirtualAccount(CreateVirtualAccountRequest request){
        log.info("dto: {}", request);
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/partner/collection/bank_transfer";
        log.info("full url: {}", url);
        HttpEntity<CreateVirtualAccountRequest> entity = new HttpEntity<>(request, headers());
        log.info("Entity: {}", entity);
        ResponseEntity<CreateVirtualAccountResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, CreateVirtualAccountResponse.class);
        return responseEntity.getBody();
    }

    public PayoutResponse payout(PayoutRequest payoutRequest){
        log.info("dto: {}", payoutRequest);
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/partner/payout/initiate_transfer";
        log.info("full url: {}", url);
        HttpEntity<PayoutRequest> entity = new HttpEntity<>(payoutRequest, headers());
        log.info("Entity: {}", entity);
        ResponseEntity<PayoutResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, PayoutResponse.class);
        return responseEntity.getBody();
    }

    public VerifyAccountResponse verifyAccount(VerifyAccountRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/partner/payout/verify_bank_account";
        log.info("Full url: {}", url);
        HttpEntity<VerifyAccountRequest> entity = new HttpEntity<>(request, headers());
        log.info("Request entity: {}", entity);
        ResponseEntity<VerifyAccountResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, VerifyAccountResponse.class);
        log.info("Response body: {}", responseEntity.getBody());
        return responseEntity.getBody();
    }


    public String webHookResponse(WebHookRequest request){
        String requestBody = request.toString();
        String secret = accountUtils.decode(privateKey, 5);
        log.info("secret key: {}", secret);

        String signature = SignatureGenerator.encryptWebHookData(requestBody, secret);
        System.out.println(signature);
        return signature;
    }

    public BanksResponse fetchAllBanks(){
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/partner/list_banks/NG";
        log.info("full url: " + url);
        HttpEntity entity = new HttpEntity<>(headers());
        ResponseEntity<BanksResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, BanksResponse.class);
        return responseEntity.getBody();
    }


    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " +accountUtils.decode(accessToken, 5));
        headers.set("moni-signature", accountUtils.decode(moniSignature, 5));

        return headers;
    }

}
