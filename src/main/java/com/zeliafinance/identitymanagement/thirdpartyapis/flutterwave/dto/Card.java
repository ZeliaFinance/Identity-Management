package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card {
    @JsonProperty("first_6digits")
    private String first6digits;
    @JsonProperty("last_4digits")
    private String last4digits;
    private String issuer;
    private String country;
    private String type;
    private String expiry;
}
