package com.zeliafinance.identitymanagement.repository;

import com.zeliafinance.identitymanagement.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    boolean existsByWalletId(String walletId);
    boolean existsByEmail(String email);
    boolean existsByBvn(String bvn);
    Optional<UserCredential> findByWalletId(String walletId);
    Optional<UserCredential> findByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    @Query("SELECT u FROM UserCredential u WHERE LOWER(u.firstName) LIKE %:key% OR LOWER(u.lastName) LIKE %:key% OR LOWER(u.email) LIKE %:key%")
    List<UserCredential> searchUsersByKey(@Param("key") String key);

    //long countByVerifiedTrue(); // Count verified users
    // long countByVerifiedFalse(); // Count unverified users
    List<UserCredential> findByEmailVerifyStatusEquals(String emailVerifyStatus);

    @Query("SELECT u FROM UserCredential u WHERE LOWER(u.firstName) LIKE %:key% OR LOWER(u.lastName) LIKE %:key% OR LOWER(u.email) LIKE %:key%")
    List<UserCredential> searchUsersByKey(@Param("key") String key);

        //long countByVerifiedTrue(); // Count verified users
       // long countByVerifiedFalse(); // Count unverified users
    List<UserCredential> findByEmailVerifyStatusEquals(String emailVerifyStatus);






}
