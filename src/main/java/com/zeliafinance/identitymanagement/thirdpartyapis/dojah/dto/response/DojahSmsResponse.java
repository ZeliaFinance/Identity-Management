package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DojahSmsResponse {
    @JsonProperty("entity")
    private List<com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.Data> entity;
}
