package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DojahSmsService {

    @Value("${dojah.app-id}")
    private String appId;
    @Value("${dojah.public-key}")
    private String publicKey;
    @Value("${dojah.private-key}")
    private String privateKey;
    @Value("${dojah.base-url}")
    private String baseUrl;

    public DojahSmsResponse sendSms(DojahSmsRequest request) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "api/v1/messaging/sms";
        log.info("full url: {}", url);
        HttpEntity<DojahSmsRequest> entity = new HttpEntity<>(request, headers());
        ResponseEntity<DojahSmsResponse> responseString = restTemplate.exchange(url, HttpMethod.POST, entity, DojahSmsResponse.class);
        DojahSmsResponse jsonResponse = responseString.getBody();
        log.info("Response: {}", responseString);

        return jsonResponse;
    }

    public DojahBvnResponse basicBvnLookUp(BvnRequest request){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "api/v1/kyc/bvn/full?bvn="+request.getBvn();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<DojahBvnResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, DojahBvnResponse.class);
        DojahBvnResponse jsonResponse = responseEntity.getBody();
        log.info("Response: {}", jsonResponse);
        return jsonResponse;
    }

    public AdvancedBvnResponse advancedBvnLookup(BvnRequest request){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "api/v1/kyc/bvn/advance?bvn="+request.getBvn();
        log.info("full url: " + url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<AdvancedBvnResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, AdvancedBvnResponse.class);
        AdvancedBvnResponse jsonResponse = responseEntity.getBody();
        log.info("Response: \n{}", jsonResponse);
        return jsonResponse;
    }

    public LookupNubanResponse nubanLookup(NubanLookupRequest request){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "api/v1/kyc/nuban?account_number="+request.getAccountNumber()+"?bank_code="+request.getBankCode();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<LookupNubanResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, LookupNubanResponse.class);
        LookupNubanResponse response = responseEntity.getBody();
        log.info("Response: {}", response);
        return response;
    }

    public BasicPhoneNumberLookUpResponse basicPhoneNumberLookup(PhoneNumberLookupRequest request){
        String url = baseUrl + "api/v1/kyc/phone_number/basic?phone_number="+request.getPhoneNumber();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<BasicPhoneNumberLookUpResponse> responseEntity = restTemplate().exchange(url, HttpMethod.GET, entity, BasicPhoneNumberLookUpResponse.class);
        BasicPhoneNumberLookUpResponse response = responseEntity.getBody();
        log.info("Response: {}", response);
        return response;
    }

    public NinLookupResponse ninLookup(NinRequest request){
        String url = baseUrl + "api/v1/kyc/nin?nin="+request.getNin();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<NinLookupResponse> responseEntity = restTemplate().exchange(url, HttpMethod.GET, entity, NinLookupResponse.class);
        log.info("response: \n{}", responseEntity.getBody());
        return responseEntity.getBody();
    }

    public VNinResponse vninLookup(VNinRequest request){
        String url = baseUrl + "api/v1/kyc/vnin?vnin="+request.getVnin();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<VNinResponse> responseEntity = restTemplate().exchange(url, HttpMethod.GET, entity, VNinResponse.class);
        return responseEntity.getBody();
    }

    private RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("AppId", appId);
        headers.set("Authorization", privateKey);
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
