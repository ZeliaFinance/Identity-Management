package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Data {
    private String status;
    private String mobile;
    @JsonProperty("message_id")
    private String messageId;
    @JsonProperty("reference_id")
    private String referenceId;
}
