package com.zeliafinance.identitymanagement.loan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Repayments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loanRefNo;
    private String loanTenor;
    private String nextRepaymentDate;
    private String repaymentStatus;
    private String loanType;
    private String loanStatus;
}
