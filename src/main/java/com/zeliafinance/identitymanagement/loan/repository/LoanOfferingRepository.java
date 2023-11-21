package com.zeliafinance.identitymanagement.loan.repository;

import com.zeliafinance.identitymanagement.loan.entity.LoanOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanOfferingRepository extends JpaRepository<LoanOffering, Long> {
    List<LoanOffering> findByLoanProduct(String loanProduct);
    List<Integer> findByLoanProductAndInterestRateAndMinAmountAndMaxAmount(String loanProduct, double minAmount, double maxAmount, double interestRate);
}
