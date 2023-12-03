package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebHookRequest {
    private String event;
    private WebHookData data;
    private String pay_amount;
    private String pay_method;
    private String holder_phone;
    private String holder_phone_carrier;
    private Object order_details;
    private String holder_currency;
    private String holder_country_code;
    private String pay_status;
    private String holder_account_number;
    private String pub_date;
    private String modified_date;
    private String holder_bank_name;
    private String merch_currency;
    private String merch_amount;
}
