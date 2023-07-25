package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdvancedBvnData {
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
    private String email;
    @JsonProperty("enrollment_bank")
    private String enrollmentBank;
    @JsonProperty("enrollment_branch")
    private String enrollmentBranch;
    @JsonProperty("level_of_account")
    private String levelOfAccount;
    @JsonProperty("lga_of_origin")
    private String lgaOfOrigin;
    @JsonProperty("lga_of_residence")
    private String lgaOfResidence;
    @JsonProperty("marital_status")
    private String maritalStatus;
    @JsonProperty("name_on_card")
    private String nameOnCard;
    private String nationality;
    private String nin;
    @JsonProperty("phone_number2")
    private String phoneNumber2;
    @JsonProperty("registration_date")
    private String registrationDate;
    @JsonProperty("registration_address")
    private String registrationAddress;
    @JsonProperty("state_of_origin")
    private String stateOfOrigin;
    @JsonProperty("state_of_residence")
    private String stateOfResidence;
    private String title;
    @JsonProperty("watch_listed")
    private String watchListed;
}
