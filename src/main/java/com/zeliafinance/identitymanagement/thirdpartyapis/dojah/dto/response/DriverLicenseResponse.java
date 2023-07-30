package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.DriverLicenseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverLicenseResponse {
    private DriverLicenseData entity;
}
