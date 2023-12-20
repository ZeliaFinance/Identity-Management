package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request.CACBasicRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CACBasicResponse {

    private CACBasicRequest cacBasicRequest;

    @JsonProperty("rc_number")
    private String rcNumber;

    @JsonProperty("company_name")
    private String companyName;

    private String address;

    @JsonProperty("date_of_registration")
    private String dateOfRegistration;

    @JsonProperty("type_of_company")
    private String typeOfCompany;
}
