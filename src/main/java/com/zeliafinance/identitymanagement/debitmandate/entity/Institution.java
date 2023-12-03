package com.zeliafinance.identitymanagement.debitmandate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String studentFirstName;
    private String studentLastName;
    private String facultyName;
    private String matriculationNumber;
    private String beneficiaryName;
    private String institutionName;
    private String institutionBankName;
    private String institutionAccountNumber;
    private String departmentName;
    private String loanRefNo;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private String createdBy;
}
