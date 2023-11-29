package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayoutRequest {
    private String payout_step;
    private String receiver_currency;
    private String receiver_amount;
    private String transfer_method;
    private String transfer_receiver_type;
    private String receiver_account_num;
    private String receiver_country_code;
    private String receiver_sort_code;
    private String receiver_account_name;
    private String sender_amount;
    private String sender_currency;
    private String transfer_note;
}
