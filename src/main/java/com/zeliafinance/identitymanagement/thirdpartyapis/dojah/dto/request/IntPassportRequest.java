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
public class IntPassportRequest {
    @JsonProperty("passport_number")
    private String passportNumber;
    private String surname;
}
