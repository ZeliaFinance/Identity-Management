package com.zeliafinance.identitymanagement.loan.entity;

import com.zeliafinance.identitymanagement.entity.UserCredential;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private double interestRate;
    private double interest;
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
    private String repaymentStatus;
    private double amountRepaid;
    private LocalDateTime nextRepaymentDate;
    private String nextRepaymentStatus;
    @OneToOne
    private LoanProduct loanProduct;
    @ManyToOne
    private UserCredential loanApplicant;
    private LocalDateTime dateDisbursed;
    private double amountDisbursed;
    private String matriculationNumber;
}
