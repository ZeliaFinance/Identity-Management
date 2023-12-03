package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateVirtualAccountRequest {
    private String pay_va_step;
    private String country_code;
//    private String pay_amount;
    private String holder_account_type;
    private String holder_legal_number;
    private String pay_currency;
    private String bank_name;
    private String customer_ref;
    private String pay_ext_ref;
}
