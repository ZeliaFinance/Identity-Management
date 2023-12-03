package com.zeliafinance.identitymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionRef;
    private String walletId;
    private String transactionType;
    private double transactionAmount;
    private LocalDateTime createdAt;
    private String transactionStatus;
    private String externalRefNumber;
}
