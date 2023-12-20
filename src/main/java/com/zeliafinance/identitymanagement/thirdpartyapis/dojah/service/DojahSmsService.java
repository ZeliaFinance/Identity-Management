package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.service;

import com.zeliafinance.identitymanagement.dto.OtpDto;
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
    @Value("${dojah.api.authorization}")
    private String authorization;

    public DojahSmsResponse sendSms(DojahSmsRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "api/v1/messaging/sms";
        log.info("full url: {}", url);
        HttpEntity<DojahSmsRequest> entity = new HttpEntity<>(request, headers());
        ResponseEntity<DojahSmsResponse> responseString = restTemplate.exchange(url, HttpMethod.POST, entity, DojahSmsResponse.class);
        DojahSmsResponse jsonResponse = responseString.getBody();
        log.info("Response: {}", jsonResponse);

        return jsonResponse;
    }

    public OtpResponse sendOtp(OtpRequest otpRequest){
        String url = baseUrl + "api/v1/messaging/otp";
        log.info("full url: {}", url);
        HttpEntity<OtpRequest> entity = new HttpEntity<>(otpRequest, headers());
        ResponseEntity<OtpResponse> responseEntity = restTemplate().exchange(url, HttpMethod.POST, entity, OtpResponse.class);
        return responseEntity.getBody();
    }

    public ValidateOtpResponse validateOtp(ValidateOtpRequest otpRequest){
        String url = baseUrl + "api/v1/messaging/otp/validate";
        log.info("full url: {}", url);
        HttpEntity<ValidateOtpRequest> entity = new HttpEntity<>(otpRequest, headers());
        ResponseEntity<ValidateOtpResponse> responseEntity = restTemplate().exchange(url, HttpMethod.POST, entity, ValidateOtpResponse.class);
        return responseEntity.getBody();
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

    public DriverLicenseResponse dlLookup(DriverLicenseRequest request){
        String url = baseUrl + "api/v1/kyc/dl?license_number="+request.getLicenseNumber();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<DriverLicenseResponse> responseEntity = restTemplate().exchange(url, HttpMethod.GET, entity, DriverLicenseResponse.class);
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

    public PvcResponse pvcLookup(PvcRequest request){
        String url = baseUrl + "api/v1/kyc/vin?vin="+request.getVin();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<PvcResponse> responseEntity = restTemplate().exchange(url, HttpMethod.GET, entity, PvcResponse.class);
        return responseEntity.getBody();
    }

    public IntPassportResponse intPassportLookup(IntPassportRequest request){
        String url = baseUrl + "api/v1/kyc/passport?passport_number="+request.getPassportNumber()+"?surname="+request.getSurname();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<IntPassportResponse> responseEntity = restTemplate().exchange(url, HttpMethod.GET, entity, IntPassportResponse.class);
        return responseEntity.getBody();
    }

    /*   public LookupNubanResponse nubanLookup(NubanLookupRequest request){
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

 */
    public CACBasicResponse cacBasicResponse(CACBasicRequest cacBasicRequest){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "/api/v1/kyc/cac?rc_number="+cacBasicRequest.getRcNumber()+"?company_name="+cacBasicRequest.getCompanyName();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<CACBasicResponse> responseEntity = restTemplate.exchange
                (url, HttpMethod.GET, entity, CACBasicResponse.class);
        CACBasicResponse cacBasicResponse = responseEntity.getBody();
        log.info("Response: {}", cacBasicResponse);
        return cacBasicResponse;
    }

    public CACAdvanceResponse cacAdvanceResponse(CACAdvanceRequest cacAdvanceRequest){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "/api/v1/kyc/cac/advance?rc="+cacAdvanceRequest.getRc()+"?class="
                +cacAdvanceRequest.getClass()+"?type="+cacAdvanceRequest.getType();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<CACAdvanceResponse> responseEntity = restTemplate.exchange
                (url, HttpMethod.GET, entity, CACAdvanceResponse.class);
        CACAdvanceResponse cacAdvanceResponse = responseEntity.getBody();
        log.info("Response: {}", cacAdvanceResponse);
        return cacAdvanceResponse;
    }

    public LookUpUserAccountResponse lookUpUserAccountResponse(LookUpUserAccountRequest lookUpUserAccountRequest){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String url = baseUrl + "/api/v1/kyc/accounts?mobile_number="+lookUpUserAccountRequest.getMobileNumber()+
                "?bvn="+lookUpUserAccountRequest.getBvn();
        log.info("full url: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(headers());
        ResponseEntity<LookUpUserAccountResponse> responseEntity = restTemplate.exchange
                (url, HttpMethod.GET, entity, LookUpUserAccountResponse.class);
        LookUpUserAccountResponse lookUpUserAccountResponse = responseEntity.getBody();
        log.info("Response: {}", lookUpUserAccountResponse);
        return lookUpUserAccountResponse;







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
