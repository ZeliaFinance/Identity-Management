package com.zeliafinance.identitymanagement.banks.repository;

import com.zeliafinance.identitymanagement.banks.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Bank findByBankName(String bankName);
}
