package com.zeliafinance.identitymanagement.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileRequest {
    private String firstName;
    private String lastName;
    private String otherName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String mobileNumber;
    private String whatsAppNumber;
    private String gender;
    private String bvn;
    private String nin;
    private String pin;
    private String confirmPin;
    private String role;
    private String liveLocation;
    private String deviceIp;
    @Nullable
    private String referredBy;
    private String code;
}
