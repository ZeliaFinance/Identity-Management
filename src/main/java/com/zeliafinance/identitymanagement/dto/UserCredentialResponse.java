package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentialResponse {
    private String firstName;
    private String lastName;
    private String otherName;
    private LocalDate dateOBirth;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String whatsAppNumber;
    private String gender;
    private String bvn;
    private String identityType;
    private String identityNumber;
    private String pin;
    private double accountBalance;
    private String role;
}
