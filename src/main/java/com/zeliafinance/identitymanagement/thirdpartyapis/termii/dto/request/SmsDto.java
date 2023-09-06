package com.zeliafinance.identitymanagement.thirdpartyapis.termii.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsDto {
    @JsonProperty("api_key")
    private String apiKey;
    private String to;
    private String from;
    private String sms;
    private String type;
    private String dnd;
}
