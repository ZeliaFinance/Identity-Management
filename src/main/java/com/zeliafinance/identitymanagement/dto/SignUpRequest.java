package com.zeliafinance.identitymanagement.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    @Email(message = "Enter a valid email")
    private String email;
    private String password;
    private String confirmPassword;
    private String referredBy;
}
