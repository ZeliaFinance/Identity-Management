package com.zeliafinance.identitymanagement.loan.repository;

import com.zeliafinance.identitymanagement.loan.entity.Repayments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentsRepository extends JpaRepository<Repayments, Long> {
    List<Repayments> findByLoanRefNo(String loanRefNo);
}
