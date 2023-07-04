package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.config.JwtTokenProvider;
import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private UserCredentialRepository userCredentialRepository;
    private AccountUtils accountUtils;
    private ModelMapper modelMapper;
    private EmailService emailService;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;




    public CustomResponse signUp(SignUpRequest request){
        boolean isEmailExist = userCredentialRepository.existsByEmail(request.getEmail());
        if (isEmailExist){
            return CustomResponse.builder()
                    .responseCode(AccountUtils.EMAIL_EXISTS_CODE)
                    .responseMessage(AccountUtils.EMAIL_EXISTS_MESSAGE)
                    .build();
        }
        String password = accountUtils.generatePassword();
        log.info(password + "password");
        UserCredential userCredential = UserCredential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(password))
                .build();

        UserCredential savedUser = userCredentialRepository.save(userCredential);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(request.getEmail())
                .subject("ACCOUNT CREDENTIALS")
                .messageBody("Below are your account credentials: Kindly change your password!\nEmail: " + request.getEmail() + " \nPassword: " + password  )
                .build();
        emailService.sendEmailAlert(emailDetails);

        Object response = modelMapper.map(savedUser, UserCredentialResponse.class);
        return CustomResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage("Temporary Password " + password)
                .responseBody(response)
                .build();
    }

    public CustomResponse updateUserProfile(Long userId, UserProfileRequest request){
        boolean isUserExist = userCredentialRepository.existsById(userId);
        UserCredential userCredential = userCredentialRepository.findById(userId).orElseThrow();
        if (isUserExist){
            userCredential.setFirstName(request.getFirstName());
            userCredential.setLastName(request.getLastName());
            userCredential.setOtherName(request.getOtherName());
            userCredential.setDateOBirth(request.getDateOfBirth());
            userCredential.setEmail(userCredential.getEmail());
            userCredential.setPassword(passwordEncoder.encode(request.getPassword()));
            userCredential.setPhoneNumber(request.getPhoneNumber());
            userCredential.setMobileNumber(request.getMobileNumber());
            userCredential.setWhatsAppNumber(request.getWhatsAppNumber());
            userCredential.setGender(request.getGender());
            userCredential.setBvn(request.getBvn());
            userCredential.setIdentityType(request.getIdentityType());
            userCredential.setIdentityNumber(request.getIdentityNumber());
            userCredential.setPin(request.getPin());
            userCredential.setWalletId(accountUtils.generateAccountNumber());
            userCredential.setAccountStatus("PENDING");


            UserCredential updatedUser = userCredentialRepository.save(userCredential);

            //Sending email alert
            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(updatedUser.getEmail())
                    .subject(AccountUtils.ACCOUNT_CREATION_ALERT_SUBJECT)
                    .messageBody("Congratulations! Your account has been successfully created. " +
                            "\nFind your account details below: \nAccount Name: " + updatedUser.getFirstName() +
                            " " + updatedUser.getLastName() + " " + updatedUser.getOtherName() +
                            " \nAccount Balance: " + updatedUser.getAccountBalance())
                    .build();
            emailService.sendEmailAlert(emailDetails);

            //building response object
            Object response = modelMapper.map(updatedUser, UserCredentialResponse.class);
            return CustomResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                    .responseBody(response)
                    .build();

        }

        return CustomResponse.builder()
                .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                .responseBody(null)
                .build();
    }

//    public CustomResponse updateUserRole(@RequestBody UpdateRoleRequest request, Long userId){
//        boolean isUserExist = userCredentialRepository.existsById(userId);
//        UserCredential userCredential = userCredentialRepository.findById(userId).get();
//        if (isUserExist){
//            userCredential.setRole(request.getRole());
//            UserCredential updateUser = userCredentialRepository.save(userCredential);
//            Object response = modelMapper.map(updateUser, UserCredentialResponse.class);
//            return CustomResponse.builder()
//                    .responseCode(AccountUtils.USER_ROLE_SET_CODE)
//                    .responseMessage(AccountUtils.USER_ROLE_SET_MESSAGE)
//                    .responseBody(response)
//                    .build();
//        }
//
//        return CustomResponse.builder()
//                .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
//                .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
//                .responseBody(null)
//                .build();
//    }

    public CustomResponse login(LoginDto loginDto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authentication.getName();
        authentication.getCredentials();
        log.info(authentication.getName());


        EmailDetails loginAlert = EmailDetails.builder()
                .subject("YOU'RE LOGGED IN!")
                .recipient(loginDto.getEmail())
                .messageBody("You logged into your account!!!")
                .build();
        emailService.sendEmailAlert(loginAlert);

        return CustomResponse.builder()
                .responseCode(AccountUtils.LOGIN_SUCCESS_CODE)
                .responseMessage(AccountUtils.LOGIN_SUCCESS_MESSAGE)
                .responseBody(jwtTokenProvider.generateToken(authentication))
                .build();
    }
}
