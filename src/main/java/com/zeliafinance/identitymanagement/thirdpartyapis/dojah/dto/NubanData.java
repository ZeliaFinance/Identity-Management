package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Data
public class NubanData {
    @JsonProperty("account_currency")
    private String accountCurrency;
    @JsonProperty("account_name")
    private String accountName;
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("account_type")
    private String accountType;
    @JsonProperty("address_1")
    private String address1;
    @JsonProperty("address_2")
    private String address2;
    private String city;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("country_of_birth")
    private String countryOfBirth;
    @JsonProperty("country_of_issue")
    private String countryOfIssue;
    private String dob;
    @JsonProperty("expiry_date")
    private String expiryDate;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("identity_number")
    private String identityNumber;
    @JsonProperty("identity_type")
    private String identityType;
    @JsonProperty("last_name")
    private String lastName;
    private String nationality;
    @JsonProperty("other_names")
    private String otherNames;
    private String phone;
    @JsonProperty("postal_code")
    private String postalCode;
    @JsonProperty("state_code")
    private String stateCode;
}
