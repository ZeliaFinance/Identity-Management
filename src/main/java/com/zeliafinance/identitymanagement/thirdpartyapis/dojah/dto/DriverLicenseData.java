package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto;

import lombok.Data;

@Data
public class DriverLicenseData {
    private String uuid;
    private String licenseNo;
    private String firstName;
    private String lastName;
    private String middleName;
    private String gender;
    private String issuedDate;
    private String expiryDate;
    private String stateOfIssue;
    private String birthDate;
    private String photo;
}
