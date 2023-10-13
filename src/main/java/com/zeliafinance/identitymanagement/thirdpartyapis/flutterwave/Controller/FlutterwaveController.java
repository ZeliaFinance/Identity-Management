package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.Controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.request.ChargeCardRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto.response.ChargeCardResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.service.FlutterwaveService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/flutter")
public class FlutterwaveController {

    private FlutterwaveService service;

    @PostMapping
    public ChargeCardResponse chargeCard(@RequestBody ChargeCardRequest request){
        return service.chargeCard(request);
    }
}
