package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetNipAccountRequest {
    private String accountNumber;
    private String beneficiaryBank;
    private String userName;
    private String password;
}
