package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReservedAccountRequest {
    @JsonProperty("account_name")
    private String accountName;
    private String bvn;
}
