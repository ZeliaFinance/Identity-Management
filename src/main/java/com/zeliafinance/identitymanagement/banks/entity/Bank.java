package com.zeliafinance.identitymanagement.banks.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bank")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String provider;
    private String bankCode;
    private String bankName;
    private String listCode;
    private LocalDateTime createdAt;
    private String createdBy;
}
