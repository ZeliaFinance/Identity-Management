package com.zeliafinance.identitymanagement.debitmandate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstitutionRequest {
    private String studentFirstName;
    private String studentLastName;
    private String facultyName;
    private String matriculationNumber;
    private String departmentName;
    private String beneficiaryName;
    private String institutionName;
    private String institutionBankName;
    private String institutionAccountNumber;
}
