package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Authorisation {
    private String authorization_code;
    private String bin;
    private String last4;
    private String exp_month;
    private String exp_year;
    private String channel;
    private String bank;
    private String country_code;
    private String brand;
    private boolean reusable;
    private String signature;
    private String account_name;
    private String card_type;
}
