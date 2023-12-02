package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.CreateChargeRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.CreateRefundRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateChargeResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateFundResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.service.PayStackService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/paystack")
public class PayStackController {

    private final PayStackService payStackService;

    @PostMapping("/createCharge")
    public CreateChargeResponse chargeCard(@RequestBody CreateChargeRequest createChargeRequest){
        return payStackService.createCard(createChargeRequest);
    }

    @GetMapping("/{reference}")
    public CreateChargeResponse verifyTransaction(@PathVariable String reference){
        return payStackService.verifyTransaction(reference);
    }

    @PostMapping("/refund")
    public CreateFundResponse refund(@RequestBody CreateRefundRequest request){
        return payStackService.refundAccount(request);
    }
}
