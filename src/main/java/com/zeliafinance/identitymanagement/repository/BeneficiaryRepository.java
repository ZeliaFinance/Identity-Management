package com.zeliafinance.identitymanagement.repository;

import com.zeliafinance.identitymanagement.dto.BeneficiaryRequest;
import com.zeliafinance.identitymanagement.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    BeneficiaryRequest findByWalletId(String walletId);
}
