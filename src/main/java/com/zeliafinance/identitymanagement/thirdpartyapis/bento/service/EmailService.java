package com.zeliafinance.identitymanagement.thirdpartyapis.bento.service;

import com.zeliafinance.identitymanagement.thirdpartyapis.bento.dto.EmailRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.bento.dto.EmailResponse;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
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
public class EmailService {

    @Value("${bento.baseUrl}")
    private String baseUrl;
    @Value("${bento.secretKey}")
    private String password;
    @Value("${bento.publicKey}")
    private String username;
    @Value("${bento.siteKey}")
    private String siteKey;
    AccountUtils accountUtils;

    @Autowired
    public EmailService(AccountUtils accountUtils){
        this.accountUtils = accountUtils;
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public EmailResponse sendEmail(EmailRequest request){
        String url = baseUrl + "api/v1/batch/emails?site_uuid="+accountUtils.decode(siteKey, 3);
        log.info("Full Url: {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(accountUtils.decode(username, 3), accountUtils.decode(password, 3));
        HttpEntity<EmailRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<EmailResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, EmailResponse.class);
        log.info("Response: {}", response);
        return response.getBody();
        //1a681e8737c1ca89a4b5e96eeff44cef
    }

}
