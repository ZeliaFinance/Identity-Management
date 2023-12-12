package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ChangePinPassword {

    private final UserCredentialRepository userCredentialRepository;
    private final AuthService authService;
    private final ModelMapper modelMapper;
    private final AccountUtils accountUtils;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<CustomResponse> checkSecurityQuestionResponse(ChangePinRequest changePinRequest){
        log.info("Request: {}", changePinRequest);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Email: {}", email);
        UserCredentialResponse userCredentialResponse = userCredentialResponse(email);
        log.info("UserCredential Response: {}\nSecurity Answer: {}", userCredentialResponse.getSecurityQuestion(), userCredentialResponse.getSecurityAnswer());
        if (!changePinRequest.getResponseToSecurityQuestion().equalsIgnoreCase(userCredentialResponse.getSecurityAnswer())){
             return ResponseEntity.badRequest().body(CustomResponse.builder()
                             .statusCode(400)
                             .responseMessage("Response to Security Question is wrong")
                     .build());
        }
        authService.sendOtp(OtpDto.builder()
                        .email(email)
                .build());
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.OTP_SENT_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> changePin(ChangePinRequest changePinRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredentialResponse userCredentialResponse = userCredentialResponse(email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        CustomResponse validateOtpResponse = authService.validateOtp(ValidateOtpDto.builder()
                        .email(email)
                        .otp(changePinRequest.getOtp())
                .build()).getBody();

        assert validateOtpResponse != null;
        if (!validateOtpResponse.getOtpStatus()){
            ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.INVALID_OTP_MESSAGE)
                    .build());
        }

        if (!changePinRequest.getCurrentPin().equals(accountUtils.decodePin(userCredentialResponse.getPin()))){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Incorrect Pin")
                    .build());
            }
            if (changePinRequest.getNewPin().equals(accountUtils.decodePin(userCredentialResponse.getPin()))){
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                                .statusCode(400)
                                .responseMessage("You've already used this pin.")
                        .build());
            }
            if (!changePinRequest.getNewPin().equals(changePinRequest.getConfirmNewPin())){
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                                .statusCode(400)
                                .responseMessage(AccountUtils.PIN_DISPARITY_MESSAGE)
                        .build());
            }
            userCredential.setPin(accountUtils.encodePin(changePinRequest.getNewPin()));
            userCredential = userCredentialRepository.save(userCredential);
            userCredentialResponse = modelMapper.map(userCredential, UserCredentialResponse.class);
            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage(AccountUtils.PIN_SETUP_SUCCESS_MESSAGE)
                    .responseBody(userCredentialResponse)
                    .build());


    }

    public ResponseEntity<CustomResponse> changePassword(PasswordChangeRequest passwordChangeRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential loggedInUser = userCredentialRepository.findByEmail(email).get();
        authService.validateOtp(ValidateOtpDto.builder()
                .otp(passwordChangeRequest.getOtp())
                .email(email)
                .build());

        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), loggedInUser.getPassword())){
            log.info("Encoded request Password: {}", passwordEncoder.encode(passwordChangeRequest.getCurrentPassword()));
            log.info("Saved Password: {}", loggedInUser.getPassword());
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Incorrect Password")
                    .build());
        }
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmNewPassword())){
            log.info("New Password: {}", passwordChangeRequest.getNewPassword());
            log.info("Confirm Password: {}", passwordChangeRequest.getConfirmNewPassword());
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.PASSWORD_INCORRECT_MESSAGE)
                    .build());
        }
        if (!accountUtils.isPasswordValid(passwordChangeRequest.getNewPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.PASSWORD_INVALID_MESSAGE)
                    .build());
        }
        loggedInUser.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        loggedInUser = userCredentialRepository.save(loggedInUser);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loggedInUser, UserCredentialResponse.class))
                .build());
    }

    public UserCredentialResponse userCredentialResponse(String email){
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        return modelMapper.map(userCredential, UserCredentialResponse.class);
    }
}
