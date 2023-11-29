package com.zeliafinance.identitymanagement.loanDisbursal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LoanDisbursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    private String walletId;
    private String loanRefNo;
    private double appliedAmount;
    private double amountDisbursed;
    private double amountToPayBack;
    private LocalDateTime dateDisbursed;
    private String disbursedBy;
}
