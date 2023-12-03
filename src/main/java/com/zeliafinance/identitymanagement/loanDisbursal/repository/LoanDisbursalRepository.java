package com.zeliafinance.identitymanagement.loanDisbursal.repository;

import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDisbursalRepository extends JpaRepository<LoanDisbursal, Long> {
    LoanDisbursal findByLoanRefNo(String loanRefNo);
    List<LoanDisbursal> findByWalletId(String walletId);
}
