package com.zeliafinance.identitymanagement.loan.repository;

import com.zeliafinance.identitymanagement.loan.entity.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
    Optional<LoanProduct> findByLoanProductName(String loanProductName);
}
