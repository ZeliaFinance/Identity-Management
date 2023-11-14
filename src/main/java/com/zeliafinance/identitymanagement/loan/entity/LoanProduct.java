package com.zeliafinance.identitymanagement.loan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loanProductName;
    private double minAmount;
    private double maxAmount;
    private int minDuration;
    private int maxDuration;
    private double interestRate;
    private String status;
    private String createdBy;
    private String modifiedBy;
    @CreationTimestamp
    private String createdAt;
    @UpdateTimestamp
    private String modifiedAt;
    private boolean minInterestRate;
//    @OneToOne
//    @MapsId
//    @JoinColumn(name = "loan_application")
//    private LoanApplication loanApplication;
}
