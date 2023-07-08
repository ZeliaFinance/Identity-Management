package com.zeliafinance.identitymanagement.api.account_generation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountGenerationRequest {
    @JsonProperty("account_name")
    private String accountName;
    private String bvn;
}
