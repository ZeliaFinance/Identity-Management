package com.zeliafinance.identitymanagement.repository;

import com.zeliafinance.identitymanagement.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    boolean existsByWalletId(String walletId);
    boolean existsByEmail(String email);
    boolean existsByBvn(String bvn);
    Optional<UserCredential> findByWalletId(String walletId);
    Optional<UserCredential> findByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

}
