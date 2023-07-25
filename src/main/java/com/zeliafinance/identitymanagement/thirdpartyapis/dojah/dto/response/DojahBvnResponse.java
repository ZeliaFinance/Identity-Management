package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.BvnData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DojahBvnResponse {
    @JsonProperty("entity")
    private BvnData entity;
}
