package com.zeliafinance.identitymanagement.loan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String walletId;
    private BigDecimal loanAmount;
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
    @CreationTimestamp
    private LocalDateTime createdAt;
    private String createdBy;
    @UpdateTimestamp
    private LocalDateTime modifiedAt;
    private String modifiedBy;
}