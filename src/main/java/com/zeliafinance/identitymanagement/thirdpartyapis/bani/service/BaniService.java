package com.zeliafinance.identitymanagement.thirdpartyapis.bani.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request.CreateCustomerDto;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response.CreateCustomerResponse;
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


    private HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " +accountUtils.decode(accessToken, 5));
        headers.set("moni-signature", accountUtils.decode(moniSignature, 5));

        return headers;
    }

}
