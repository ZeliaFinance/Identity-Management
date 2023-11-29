package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankData {
    private String bank_code;
    private String list_code;
    private String bank_name;
}
