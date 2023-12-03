package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Banks {
    private String bankCode;
    private String bankName;
}
