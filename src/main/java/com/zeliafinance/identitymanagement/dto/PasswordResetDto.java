package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetDto {
    private String otp;
    private String newPassword;
    private String confirmNewPassword;
}
