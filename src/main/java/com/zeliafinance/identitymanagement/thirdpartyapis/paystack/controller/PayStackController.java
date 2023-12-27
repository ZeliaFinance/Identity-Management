package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.controller;

import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.ChargeCardResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateChargeResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateFundResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.SubmitPinResponse;
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

    @PostMapping("chargeAuth")
    public ChargeCardResponse chargeCard(@RequestBody ChargeCardRequest chargeCardRequest){
        return payStackService.chargeCard(chargeCardRequest);
    }

    @PostMapping("submitPin")
    public SubmitPinResponse submitPin(@RequestBody SubmitPinRequest submitPinRequest){
        return payStackService.submitPin(submitPinRequest);
    }

    @PostMapping("submitOtp")
    public SubmitPinResponse submitOtp(@RequestBody SubmitOtpRequest submitOtpRequest){
        return payStackService.submitOtp(submitOtpRequest);
    }

    @PostMapping("submitPhone")
    public SubmitPinResponse submitPhone(@RequestBody SubmitPhoneRequest submitPhoneRequest){
        return payStackService.submitPhone(submitPhoneRequest);
    }

    @PostMapping("submitBirthday")
    public SubmitPinResponse submitBirthday(@RequestBody SubmitBirthDayRequest birthDayRequest){
        return payStackService.submitBirthday(birthDayRequest);
    }

    @PostMapping("submitAddress")
    public SubmitPinResponse submitAddress(@RequestBody SubmitAddressRequest addressRequest){
        return payStackService.submitAddress(addressRequest);
    }

    @GetMapping("/pendingTransaction/{reference}")
    public SubmitPinResponse pendingCharge(@PathVariable String reference){
        return payStackService.pendingTransaction(reference);
    }


}
