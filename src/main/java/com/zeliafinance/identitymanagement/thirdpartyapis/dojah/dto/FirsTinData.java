package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirsTinData {
    private String search;
    @JsonProperty("taxpayer_name")
    private String taxpayerName;
    @JsonProperty("cac_reg_number")
    private String cacRegNumber;
    private String firstin;
    private String jittin;
    @JsonProperty("tax_office")
    private String taxOffice;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String email;
}
