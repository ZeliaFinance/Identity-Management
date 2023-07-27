package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OtpData {
    @JsonProperty("reference_id")
    private String referenceId;
    private String destination;
    private String status;
}
