package com.zeliafinance.identitymanagement.thirdpartyapis.termii.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.termii.dto.request.SmsDto;
import com.zeliafinance.identitymanagement.thirdpartyapis.termii.dto.response.SmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SmsImpl {

    @Value("${termii.sender-id}")
    private String senderId;
    @Value("${termii.api-key}")
    private String apiKey;
    @Value("${termii.secret-key}")
    private String secretKey;
    @Value("${termii.base-url}")
    private String baseUrl;

    public SmsResponse sendSms(SmsDto smsDto){
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "sms/send";
        log.info("full url: {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "Application/json");
        HttpEntity<SmsDto> entity = new HttpEntity<>(smsDto, headers);
        ResponseEntity<SmsResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, SmsResponse.class);
        SmsResponse jsonResponse = responseEntity.getBody();
        log.info("json response {}", jsonResponse);
        return jsonResponse;
    }
}
