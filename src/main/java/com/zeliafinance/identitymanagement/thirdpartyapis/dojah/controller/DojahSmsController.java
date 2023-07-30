package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.service.DojahSmsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/dojah")
@AllArgsConstructor
public class DojahSmsController {

    private DojahSmsService service;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = "/sendSms", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DojahSmsResponse> sendSms(@RequestBody DojahSmsRequest request) {
        return ResponseEntity.ok(service.sendSms(request));
    }

    @GetMapping("/bvnBasicLookup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DojahBvnResponse> bvnBasicLookup(@RequestBody BvnRequest request){
        return ResponseEntity.ok(service.basicBvnLookUp(request));
    }

    @GetMapping("/bvnAdvancedLookup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AdvancedBvnResponse> advancedBvnLookup(@RequestBody BvnRequest request){
        return ResponseEntity.ok(service.advancedBvnLookup(request));
    }

    @GetMapping("/nubanLookup")
    public ResponseEntity<LookupNubanResponse> nubanLookup(@RequestBody NubanLookupRequest request){
        return ResponseEntity.ok(service.nubanLookup(request));
    }

    @GetMapping("/basicPhoneNumberLookup")
    public ResponseEntity<BasicPhoneNumberLookUpResponse> basicPhoneNumberLookup(@RequestBody PhoneNumberLookupRequest phoneNumberLookupRequest){
        return ResponseEntity.ok(service.basicPhoneNumberLookup(phoneNumberLookupRequest));
    }

    @GetMapping("/ninLookup")
    public ResponseEntity<NinLookupResponse> ninLookup(@RequestBody NinRequest request){
        return ResponseEntity.ok(service.ninLookup(request));
    }

    @GetMapping("/vninLookup")
    public ResponseEntity<VNinResponse> vninLookup(@RequestBody VNinRequest request){
        return ResponseEntity.ok(service.vninLookup(request));
    }

    @PostMapping("/sendOtp")
    public ResponseEntity<OtpResponse> sendOtp(@RequestBody OtpRequest request){
        return ResponseEntity.ok(service.sendOtp(request));
    }

    @PostMapping("/dlLookup")
    public ResponseEntity<DriverLicenseResponse> dlLookup(@RequestBody DriverLicenseRequest request){
        return ResponseEntity.ok(service.dlLookup(request));
    }

    @PostMapping("/pvcLookup")
    public ResponseEntity<PvcResponse> pvcLookup(@RequestBody PvcRequest request){
        return ResponseEntity.ok(service.pvcLookup(request));
    }

    @PostMapping("/intPassportLookup")
    public ResponseEntity<IntPassportResponse> intPassportLookup(@RequestBody IntPassportRequest request){
        return ResponseEntity.ok(service.intPassportLookup(request));
    }
}
