package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BvnData {
    private String bvn;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("middle_name")
    private String middleName;
    private String gender;
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    @JsonProperty("phone_number1")
    private String phoneNumber1;
    private String image;
    @JsonProperty("phone_number2")
    private String phoneNumber2;
}
