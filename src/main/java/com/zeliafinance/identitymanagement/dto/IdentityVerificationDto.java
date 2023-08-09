package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentityVerificationDto {
    private Long userId;
    private String identityType;
    private String identityNumber;
}
