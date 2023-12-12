package com.zeliafinance.identitymanagement.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zeliafinance.identitymanagement.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.joda.time.format.ISODateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private ISODateTimeFormat dateOBirth;
    private String email;
    @JsonIgnore
    private String password;
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
    @CreationTimestamp
    private LocalDate createdAt;
    @UpdateTimestamp
    private LocalDate modifiedAt;
    private String passwordResetToken;
    private LocalDate tokenExpiryDate;
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
    private LocalDateTime otpExpiryDate;
    private String maritalStatus;
    private Integer profileSetupLevel;
    private String address;
    private String securityQuestion;
    private String securityAnswer;
    private String imagePath;
    private String imageFileName;
    private String nuban;
    private int failedPinAttempts;
    private LocalDateTime lockoutTimeStamp;
    private String team;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime invitationLinkExpiry;
    private boolean inviteAccepted;
}
