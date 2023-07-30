package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverLicenseRequest {
    @JsonProperty("license_number")
    private String licenseNumber;
}
