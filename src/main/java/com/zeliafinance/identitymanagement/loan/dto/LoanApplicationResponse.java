package com.zeliafinance.identitymanagement.loan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zeliafinance.identitymanagement.dto.UserCredentialResponse;
import com.zeliafinance.identitymanagement.loan.entity.LoanProduct;
import com.zeliafinance.identitymanagement.loanDisbursal.dto.DisbursalRequest;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationResponse {
    private Long id;
    private String walletId;
    private double loanAmount;
    private int loanTenor;
    private String companyName;
    private String companyAddress;
    private String companyEmailAddress;
    private String cacRegistration;
    private String businessAccount;
    private String loanPurpose;
    private String debitCardDetails;
    private int numberOfLoans;
    private String loanStatus;
    private String loanType;
    private String wardFirstName;
    private String wardLastName;
    private String wardInstitutionName;
    private String applicantCategory;
    private String wardIdCard;
    private String employmentType;
    private double monthlySalary;
    private String companyIdCard;
    private String companyOfferLetter;
    private String businessBankName;
    private String accountName;
    private String transactionPin;
    private double amountToPayBack;
    private double interest;
    private double interestRate;
    private String loanRefNo;
    private String facultyName;
    private String departmentName;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private String createdBy;
    @UpdateTimestamp
    private LocalDateTime modifiedAt;
    private String modifiedBy;
    private int loanApplicationLevel;
    private String salaryAccount;
    private String salaryAccountNumber;
    private String salaryAccountName;
    private String businessName;
    private String businessAddress;
    private String businessMonthlyEarnings;
    private String businessAccountName;
    private String coSignerFirstName;
    private String coSignerLastName;
    private String coSignerEmail;
    private String coSignerEmailVerificationStatus;
    private String coSignerAddress;
    private String coSignerPhoneNumber;
    private String coSignerEmploymentType;
    private String coSignerRelationship;
    private String loanApplicationStatus;
    private String matriculationNumber;
    private List<LoanProduct> loanProduct;
    private UserCredentialResponse userDetails;
    private List<RepaymentResponse> repaymentsList;
    private DisbursalRequest loanDisbursal;
    private List<DisbursalRequest> disbursalList;
    private List<LoanOfferingResponse> loanOfferingResponses;
    private boolean canApplyForLoan;

}
