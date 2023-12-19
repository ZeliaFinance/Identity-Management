package com.zeliafinance.identitymanagement.otp.repository;

import com.zeliafinance.identitymanagement.otp.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Otp findByOwnerEmail(String otp);
}
