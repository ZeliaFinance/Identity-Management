package com.zeliafinance.identitymanagement.debitmandate.repository;

import com.zeliafinance.identitymanagement.debitmandate.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
}
