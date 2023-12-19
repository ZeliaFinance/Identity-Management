package com.zeliafinance.identitymanagement.otp.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.EmailDetails;
import com.zeliafinance.identitymanagement.otp.dto.OtpRequest;
import com.zeliafinance.identitymanagement.otp.dto.OtpResponse;
import com.zeliafinance.identitymanagement.otp.dto.OtpValidationRequest;
import com.zeliafinance.identitymanagement.otp.entity.Otp;
import com.zeliafinance.identitymanagement.otp.repository.OtpRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final AccountUtils accountUtils;

    public void sendOtp(OtpRequest otpRequest){
        String otp = accountUtils.generateOtp();
        Otp existingOtp = otpRepository.findByOwnerEmail(otpRequest.getEmail());
        if (existingOtp != null){
            otpRepository.delete(existingOtp);
        }
        log.info("Otp: {}", otp);
        emailService.sendEmailAlert(EmailDetails.builder()
                        .subject("OTP")
                        .recipient(otpRequest.getEmail())
                        .messageBody("Zelia finance Just sent you an otp. Do not disclose! " + otp + ". This otp expires in 3 minutes")
                .build());
        otpRepository.save(Otp.builder()
                .ownerEmail(otpRequest.getEmail())
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .otp(otp)
                .build());

    }

    public ResponseEntity<CustomResponse> validateOtp(OtpValidationRequest request){
        //fetch the otp
        Otp otp = otpRepository.findByOwnerEmail(request.getEmail());
        log.info("Email: {}", request.getEmail());
        if (otp == null){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("You haven't sent an otp yet")
                    .build());
        }
        //check if otp is expired
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())){
            return ResponseEntity.internalServerError().body(CustomResponse.builder()
                            .statusCode(500)
                            .responseMessage(AccountUtils.OTP_EXPIRED_MESSAGE)
                            .responseBody(OtpResponse.builder()
                                    .isOtpValid(false)
                                    .build())
                    .build());
        }

        if (!otp.getOtp().equals(request.getOtp())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.INVALID_OTP_MESSAGE)
                    .build());
        }

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.OTP_VALIDATED_MESSAGE)
                        .responseBody(OtpResponse.builder()
                                .isOtpValid(true)
                                .build())
                .build());

    }
}
