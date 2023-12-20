package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {
    private String accountDescription;
    private String bankName;
    private String bankCode;
    private String mobileNumber;
}
