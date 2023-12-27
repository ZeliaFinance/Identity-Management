package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.ChargeCardResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateChargeResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateFundResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.SubmitPinResponse;
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

    public SubmitPinResponse submitPin(SubmitPinRequest submitPinRequest){
        String url = baseUrl + "/charge/submit_pin";
        log.info("Full url: {}", url);
        log.info("Request: {}", submitPinRequest);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<SubmitPinRequest> entity = new HttpEntity<>(submitPinRequest, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<SubmitPinResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, SubmitPinResponse.class);
        log.info("Response: {}", responseEntity.getBody());
        return responseEntity.getBody();
    }

    public SubmitPinResponse submitOtp(SubmitOtpRequest submitOtpRequest){
        String url = baseUrl + "/charge/submit_otp";
        log.info("Full Url: {}", url);
        log.info("Request: {}", submitOtpRequest);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<SubmitOtpRequest> entity = new HttpEntity<>(submitOtpRequest, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<SubmitPinResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, SubmitPinResponse.class);
        log.info("Response: {}", responseEntity);
        return responseEntity.getBody();
    }

    public SubmitPinResponse submitPhone(SubmitPhoneRequest submitPhoneRequest){
        String url = baseUrl + "/charge/submit_phone";
        log.info("Full url: {}", url);
        log.info("Request: {}", submitPhoneRequest);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<SubmitPhoneRequest> entity = new HttpEntity<>(submitPhoneRequest, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<SubmitPinResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, SubmitPinResponse.class);
        log.info("Response: {}", responseEntity.getBody());
        return responseEntity.getBody();
    }

    public SubmitPinResponse submitBirthday(SubmitBirthDayRequest birthDayRequest){
        String url = baseUrl + "/charge/submit_birthday";
        log.info("Full url: {}", url);
        log.info("Request Body: {}", birthDayRequest);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<SubmitBirthDayRequest> entity = new HttpEntity<>(birthDayRequest, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<SubmitPinResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, SubmitPinResponse.class);
        log.info("Response: {} ", responseEntity);
        return responseEntity.getBody();
    }

    public SubmitPinResponse submitAddress(SubmitAddressRequest addressRequest){
        String url = baseUrl + "/charge/submit_address";
        log.info("Full Url: {}", url);
        log.info("Request: {}", addressRequest);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<SubmitAddressRequest> entity = new HttpEntity<>(addressRequest, headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<SubmitPinResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, SubmitPinResponse.class);
        log.info("Response: {}", responseEntity);
        return responseEntity.getBody();
    }

    public SubmitPinResponse pendingTransaction(String reference){
        String url = baseUrl + "/charge/"+reference;
        log.info("Full url: {}", url);
        log.info("Reference: {}", reference);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(headers());
        log.info("Headers: {}", entity.getHeaders());
        ResponseEntity<SubmitPinResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, SubmitPinResponse.class);
        log.info("Response: {}", responseEntity.getBody());
        return responseEntity.getBody();
    }


    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " +accountUtils.decode(secretKey, 5));
        return headers;
    }
}
