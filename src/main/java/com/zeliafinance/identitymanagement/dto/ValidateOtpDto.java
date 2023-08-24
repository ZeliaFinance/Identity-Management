package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidateOtpDto {
    private String otp;
    private String referenceId;
    private String email;
}
