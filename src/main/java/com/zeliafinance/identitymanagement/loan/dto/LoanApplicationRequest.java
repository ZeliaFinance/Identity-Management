package com.zeliafinance.identitymanagement.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequest {
    private double loanAmount;
    private String companyName;
    private String companyAddress;
    private String companyEmailAddress;
    private String cacRegistration;
    private String businessAccountNumber;
    private String loanPurpose;
    private String loanType;
    private String walletId;
    private int loanTenor;
    private String applicantCategory;
    private String wardFirstName;
    private String wardLastName;
    private String wardInstitutionName;
    private String wardIdCard;
    private String employmentType;
    private double monthlySalary;
    private String companyIdCard;
    private String companyOfferLetter;
    private String businessBankName;
    private String accountName;
    private String pin;
    private String confirmPin;
    private String facultyName;
    private String departName;
    private String salaryAccountName;
    private String salaryAccountNumber;
    private String salaryBankName;
    private String businessName;
    private String businessAddress;
    private String businessMonthlyEarnings;
    private String businessAccountName;
    private String coSignerFirstName;
    private String coSignerLastName;
    private String coSignerAddress;
    private String coSignerPhoneNumber;
    private String coSignerEmploymentType;
    private String coSignerRelationship;
    private String studentIdCard;
    private String matriculationNumber;
}
