package com.zeliafinance.identitymanagement.repository;

import com.zeliafinance.identitymanagement.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {
}
