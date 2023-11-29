package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyAccountResponse {
    private String account_name;
    private String account_number;
    private String bank_name;
    private String message;
    private boolean status;
    private int status_code;
}
