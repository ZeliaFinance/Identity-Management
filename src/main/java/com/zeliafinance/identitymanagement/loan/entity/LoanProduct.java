package com.zeliafinance.identitymanagement.loan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;private String loanProductName;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minDuration;
    private Integer maxDuration;
    private Integer interestRate;
    private String status;
    private String createdBy;
    private String modifiedBy;
    @CreationTimestamp
    private String createdAt;
    @UpdateTimestamp
    private String modifiedAt;
}
