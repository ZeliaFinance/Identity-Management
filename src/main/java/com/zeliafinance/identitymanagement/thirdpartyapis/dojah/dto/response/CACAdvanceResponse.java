package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.Affiliates;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request.CACAdvanceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CACAdvanceResponse {

    private CACAdvanceRequest cacAdvanceRequest;

    @JsonProperty("Branch_Address")
    private String BranchAddress;

    private String City;

    private String Classification;

    @JsonProperty("Date_of_Registration")
    private String DateOfRegistration;

    private String Email;

    @JsonProperty("Head_Office_Address")
    private String headOfficeAddress;

    private String LGA;

    @JsonProperty("Name_of_Company")
    private String nameOfCompany;

    @JsonProperty("Number_of_Affiliates")
    private String numberOfAffiliates;

    @JsonProperty("RC_Number")
    private String rcNumber;

    @JsonProperty("Share_capital")
    private String shareCapital;

    @JsonProperty("Share_capital_in_words")
    private String shareCapitalInWords;

    private String State;

    private String Status;

    @JsonProperty("Type_of_Company")
    private String typeOfCompany ;

    private List<Affiliates> affiliates;

    private String imageReport;


}
