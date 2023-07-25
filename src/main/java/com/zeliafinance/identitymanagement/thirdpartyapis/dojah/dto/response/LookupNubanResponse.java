package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.NubanData;

public class LookupNubanResponse {
    @JsonProperty("entity")
    private NubanData entity;
}
