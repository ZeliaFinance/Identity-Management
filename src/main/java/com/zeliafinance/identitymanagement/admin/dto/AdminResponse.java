package com.zeliafinance.identitymanagement.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zeliafinance.identitymanagement.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String otherName;
    private String dateOBirth;
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
    @Column(unique = true)
    private String walletId;
    private String accountStatus;
    private String createdAt;
    private String modifiedAt;
    private String passwordResetToken;
    private String tokenExpiryDate;
    private String emailVerifyStatus;
    private String deviceIp;
    private String liveLocation;
    private String modifiedby;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String referralCode;
    private String referredBy;
    private String hashedPassword;
    private String otp;
    private String referenceId;
    private String otpExpiryDate;
    private String maritalStatus;
    private Integer profileSetupLevel;
    private String address;
    private String securityQuestion;
    private String securityAnswer;
    private String imagePath;
    private String imageFileName;
    private String nuban;
    private int failedPinAttempts;
    private String lockoutTimeStamp;
    private String team;
    private String createdBy;
    private String modifiedBy;
    private String invitationLinkExpiry;
    private boolean inviteAccepted;
}
