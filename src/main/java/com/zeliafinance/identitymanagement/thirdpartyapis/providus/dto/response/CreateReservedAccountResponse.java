package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReservedAccountResponse {
    private boolean requestSuccessful;
    private String responseMessage;
    private String responseCode;
    private String bvn;
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("account_name")
    private String accountName;
}
