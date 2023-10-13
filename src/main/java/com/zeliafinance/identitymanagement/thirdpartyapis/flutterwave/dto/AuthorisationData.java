package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorisationData {
    private String mode;
    private String endpoint;
}
