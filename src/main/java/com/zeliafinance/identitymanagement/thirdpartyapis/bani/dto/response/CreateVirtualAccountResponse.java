package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateVirtualAccountResponse {
    private String message;
    private boolean status;
    private int status_code;
    private String payment_reference;
    private String holder_account_number;
    private String holder_bank_name;
    private String amount;
    private String payment_ext_reference;
    private String account_type;
    private String account_name;
    private Object custom_data;
}
