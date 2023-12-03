package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response;

import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.MetaData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseData {
    private double amount;
    private String currency;
    private String transaction_date;
    private String status;
    private String reference;
    private String domain;
    private MetaData metaData;
    private String gateway_response;
    private String message;
    private String channel;
    private String ip_address;
    private String log;
    private int fees;
    private Authorisation authorization;
    private Customer customer;
    private String plan;
}
