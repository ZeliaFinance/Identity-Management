package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PvcData {
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("voter_identification_number")
    private String voterIdentificationNumber;
    private String gender;
    private String occupation;
    @JsonProperty("time_of_registration")
    private String timeOfRegistration;
    private String state;
    @JsonProperty("local_government")
    private String localGovernment;
    @JsonProperty("registration_area_ward")
    private String registrationAreaWard;
    @JsonProperty("polling_unit")
    private String pollingUnit;
    @JsonProperty("polling_unit_code")
    private String pollingUnitCode;
    private String address;
    private String phone;
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
}
