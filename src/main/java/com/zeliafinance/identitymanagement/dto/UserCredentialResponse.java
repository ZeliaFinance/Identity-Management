package com.zeliafinance.identitymanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    private double accountBalance;
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
    private String nuban;
    private String walletId;
    private List<LoanApplicationResponse> loanApplications;
}
