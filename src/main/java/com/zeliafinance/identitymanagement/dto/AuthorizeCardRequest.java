package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorizeCardRequest {
    private String pin;
    private String otp;
    private String phone;
    private String birthday;
    private String reference;
    private String address;
    private String state;
    private String city;
    private String zipCode;
}
