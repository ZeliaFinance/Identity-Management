package com.zeliafinance.identitymanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCredentialResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String otherName;
    private String address;
    private LocalDate dateOBirth;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String whatsAppNumber;
    private String gender;
    private String bvn;
    private String bvnVerifyStatus;
    private String nin;
    private String ninStatus;
    private String pin;
    private String role;
    private String emailVerifyStatus;
    private String referralCode;
    private String referredBy;
    private String hashedPassword;
    private String maritalStatus;
    private String securityQuestion;
    private String securityAnswer;
    private Integer profileSetupLevel;
    private String imagePath;
    private String imageFileName;
    private double availableBalance;
    private String walletId;
    private String vAccountNumber;
    private double accountBalance;
    private String accountStatus;
    private String customerRef;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoggedIn;
}
