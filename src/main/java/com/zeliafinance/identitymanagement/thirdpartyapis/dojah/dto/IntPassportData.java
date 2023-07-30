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
public class IntPassportData {
    @JsonProperty("passport_number")
    private String passportNumber;
    @JsonProperty("date_of_issue")
    private String dateOfIssue;
    @JsonProperty("expiry_date")
    private String expiryDate;
    @JsonProperty("document_type")
    private String documentType;
    @JsonProperty("issue_place")
    private String issuePlace;
    private String surname;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("other_names")
    private String otherNames;
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    private String gender;
    private String photo;
}
