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
public class Customer {
    private long id;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String name;
    private String email;
    @JsonProperty("created_at")
    private String createdAt;

}
