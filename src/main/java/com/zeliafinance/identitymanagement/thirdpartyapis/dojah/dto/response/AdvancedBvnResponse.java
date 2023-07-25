package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.AdvancedBvnData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvancedBvnResponse {
    @JsonProperty("entity")
    private AdvancedBvnData entity;
}
