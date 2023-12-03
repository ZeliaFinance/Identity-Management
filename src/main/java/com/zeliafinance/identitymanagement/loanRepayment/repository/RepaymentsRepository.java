package com.zeliafinance.identitymanagement.loanRepayment.repository;

import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentsRepository extends JpaRepository<Repayments, Long> {
    List<Repayments> findByLoanRefNo(String loanRefNo);
    List<Repayments> findByWalletId(String walletId);
}
