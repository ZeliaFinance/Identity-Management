package com.zeliafinance.identitymanagement.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    @Email(message = "Enter a valid email")
    private String email;
    @Nullable
    private String password;
    @Nullable
    private String hashedPassword;
}
