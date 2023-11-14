package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceEnquiryResponse {
    private String accountStatus;
    private String emailAddress;
    private String phoneNumber;
    private String accountName;
    private String bvn;
    private String accountNumber;
    private String cbaCustomerID;
    private String responseMessage;
    private String availableBalance;
    private String responseCode;
}
