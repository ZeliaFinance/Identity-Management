package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PinSetupDto {
    private String email;
    private String pin;
    private String confirmPin;
}
