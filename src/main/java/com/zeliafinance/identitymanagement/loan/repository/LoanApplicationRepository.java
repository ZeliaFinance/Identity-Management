package com.zeliafinance.identitymanagement.loan.repository;

import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Optional<List<LoanApplication>> findByWalletId(String walletId);
    boolean existsByWalletId(String walletId);
    Optional<LoanApplication> findByLoanRefNo(String loanRefNo);
    Optional<List<LoanApplication>> findByLoanApplicationStatus(String loanApplicationStatus);

}
