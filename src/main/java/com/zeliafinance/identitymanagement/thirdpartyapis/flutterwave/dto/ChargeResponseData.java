package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeResponseData {
    private long id;
    @JsonProperty("tx_ref")
    private String txRef;
    @JsonProperty("flw_ref")
    private String flwRef;
    private String deviceFingerPrint;
    private int amount;
    @JsonProperty("charged_amount")
    private int chargedAmount;
    @JsonProperty("app_fee")
    private double appFee;
    @JsonProperty("merchant_fee")
    private int merchantFee;
    @JsonProperty("processor_response")
    private String processorResponse;
    @JsonProperty("auth_model")
    private String authModel;
    private String currency;
    private String ip;
    private String narration;
    private String status;
    @JsonProperty("auth_url")
    private String authUrl;
    @JsonProperty("payment_type")
    private String paymentType;
    @JsonProperty("fraud_status")
    private String fraudStatus;
    @JsonProperty("charge_type")
    private String chargeType;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("account_id")
    private String accountId;
    private Customer customer;
    private Card card;


}
