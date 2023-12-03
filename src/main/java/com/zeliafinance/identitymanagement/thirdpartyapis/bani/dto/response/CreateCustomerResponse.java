package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomerResponse {

    private String message;
    private boolean status;

    @JsonProperty("status_code")
    private int statusCode;

    @JsonProperty("customer_ref")
    private String customerRef;
}
