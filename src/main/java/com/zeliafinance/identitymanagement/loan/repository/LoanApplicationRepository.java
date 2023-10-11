package com.zeliafinance.identitymanagement.loan.repository;

import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Optional<LoanApplication> findByWalletId(String walletId);
}
