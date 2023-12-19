package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.ChargeCardRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.CreateChargeRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.CreateRefundRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.ChargeCardResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateChargeResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateFundResponse;
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
public class PayStackService {

    @Value("${paystack.secretKey}")
    private String secretKey;

    @Value("${paystack.baseUrl}")
    private String baseUrl;

    @Autowired
    private final AccountUtils accountUtils;

    public CreateChargeResponse createCard(CreateChargeRequest request){
        String url = baseUrl + "/charge";
        log.info("full url: {}", url);
        log.info("Request: {}", request);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<CreateChargeRequest> entity = new HttpEntity<>(request, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<CreateChargeResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, CreateChargeResponse.class);
        log.info("Response body: {}", responseEntity);
        return responseEntity.getBody();
    }

    public CreateChargeResponse verifyTransaction(String reference){

        String url = baseUrl + "transaction/verify/"+reference;

        log.info("full url: {}", url);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<CreateChargeResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, CreateChargeResponse.class);
        log.info("Response Body: {}", responseEntity);
        return responseEntity.getBody();
    }

    public CreateFundResponse refundAccount(CreateRefundRequest request){
        String url = baseUrl + "/refund";
        log.info("full url: {}", url);
        log.info("Request Body: {}", request);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<CreateRefundRequest> entity = new HttpEntity<>(request, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<CreateFundResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, CreateFundResponse.class);
        log.info("Response Body: {}", responseEntity);
        return responseEntity.getBody();
    }

    public ChargeCardResponse chargeCard(ChargeCardRequest chargeCardRequest){
        String url = baseUrl + "/transaction/charge_authorization";
        log.info("full url: {}", chargeCardRequest);
        log.info("Request Body: {}", chargeCardRequest);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ChargeCardRequest> entity = new HttpEntity<>(chargeCardRequest, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<ChargeCardResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ChargeCardResponse.class);
        log.info("Response Body: {}", response);
        return response.getBody();
    }

    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " +accountUtils.decode(secretKey, 5));
        return headers;
    }
}
