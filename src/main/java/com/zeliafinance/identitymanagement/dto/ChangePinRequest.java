package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePinRequest {
    private String responseToSecurityQuestion;
    private String otp;
    private String currentPin;
    private String newPin;
    private String confirmNewPin;
}
