package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.Bank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LookUpUserAccountResponse {
    private List<Bank> banks;

    private String bvn;

    @JsonProperty("mobile_number")
    private String mobileNumber;
}
