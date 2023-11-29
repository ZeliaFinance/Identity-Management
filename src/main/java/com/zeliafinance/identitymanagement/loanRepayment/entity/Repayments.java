package com.zeliafinance.identitymanagement.loanRepayment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

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
    private int loanTenor;
    private LocalDateTime nextRepaymentDate;
    private String repaymentStatus;
    private double amountPaid;
}
