package com.zeliafinance.identitymanagement.debitmandate.repository;

import com.zeliafinance.identitymanagement.debitmandate.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
