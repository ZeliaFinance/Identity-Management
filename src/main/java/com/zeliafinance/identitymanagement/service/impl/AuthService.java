package com.zeliafinance.identitymanagement.service.impl;

import com.google.common.cache.LoadingCache;
import com.zeliafinance.identitymanagement.config.JwtTokenProvider;
import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.Role;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;


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


    public ResponseEntity<CustomResponse> signUp(SignUpRequest request){
        boolean isEmailExist = userCredentialRepository.existsByEmail(request.getEmail());
        if (isEmailExist){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .responseCode(AccountUtils.EMAIL_EXISTS_CODE)
                    .responseMessage(AccountUtils.EMAIL_EXISTS_MESSAGE)
                    .build());
        }
        if (!request.getPassword().equals(request.getConfirmPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.PASSWORD_INCORRECT_CODE)
                            .responseBody(AccountUtils.PASSWORD_INCORRECT_MESSAGE)
                    .build());
        }

        UserCredential userCredential = UserCredential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .walletId(accountUtils.generateAccountNumber())
                .emailVerifyStatus("UNVERIFIED")
                .build();

        userCredential.setRole(Role.ROLE_USER);
        UserCredential savedUser = userCredentialRepository.save(userCredential);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(request.getEmail())
                .subject(AccountUtils.ACCOUNT_CONFIRMATION_SUBJECT)
                .messageBody(AccountUtils.ACCOUNT_CONFIRMATION_MESSAGE)
                .build();
        emailService.sendEmailAlert(emailDetails);

        Object response = modelMapper.map(savedUser, UserCredentialResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .responseBody(response)
                .build());
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
            userCredential.setAccountStatus("PENDING");
            userCredential.setEmailVerifyStatus("VERIFIED");
            userCredential.setDeviceIp(request.getDeviceIp());
            userCredential.setLiveLocation(request.getLiveLocation());
            //boolean isRoleExists = roleRepository.existsByRoleName(request.getRole());


            if (!userCredential.getEmailVerifyStatus().equalsIgnoreCase("VERIFIED")){
                EmailDetails emailDetails = EmailDetails.builder()
                        .recipient(userCredential.getEmail())
                        .subject(AccountUtils.ACCOUNT_CREATION_ALERT_SUBJECT)
                        .messageBody("Congratulations! Your account has been successfully created. " +
                                "\nFind your account details below: \nAccount Name: " + request.getFirstName() +
                                " " + request.getLastName() + " " + request.getOtherName())
                        .build();
                emailService.sendEmailAlert(emailDetails);

            }


            UserCredential updatedUser = userCredentialRepository.save(userCredential);

            //Sending email alert

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

    public CustomResponse fetchAllUsers(int pageNo, int pageSize){

        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<UserCredential> userCredentials = userCredentialRepository.findAll(pageable);
        List<UserCredential> list = userCredentials.getContent();

        Object response = list.stream().map(user -> modelMapper.map(user, UserCredentialResponse.class)).collect(Collectors.toList());


        return CustomResponse.builder()
                .responseCode(String.valueOf(HttpStatus.OK))
                .responseMessage("SUCCESS")
                .responseBody(response)
                .info(Info.builder()
                        .totalPages(userCredentials.getTotalPages())
                        .totalElements(userCredentials.getTotalElements())
                        .pageSize(userCredentials.getSize())
                        .build())

                .build();

    }


    public CustomResponse fetchUser(Long userId){
        boolean isUserExists = userCredentialRepository.existsById(userId);
        if (!isUserExists){
            nonExistentUserById();
        }
        UserCredential userCredential = userCredentialRepository.findById(userId).orElseThrow();
        Object response = modelMapper.map(userCredential, UserCredentialResponse.class);
        return CustomResponse.builder()
                .responseCode(String.valueOf(HttpStatus.valueOf("OK")))
                .responseMessage(HttpStatus.OK.name())
                .responseBody(response)
                .build();
    }

    public CustomResponse updateUserRole(Long userId){
        boolean isUserExist = userCredentialRepository.existsById(userId);
        if (!isUserExist){
            nonExistentUserById();
        }
        UserCredential userCredential = userCredentialRepository.findById(userId).orElseThrow();
        userCredential.setRole(Role.ROLE_ADMIN);

        userCredentialRepository.save(userCredential);
        Object response = modelMapper.map(userCredential, UserCredentialResponse.class);
        return CustomResponse.builder()
                .responseCode(AccountUtils.USER_ROLE_SET_CODE)
                .responseBody(AccountUtils.USER_ROLE_SET_MESSAGE)
                .responseBody(response)
                .info(null)
                .build();

    }

    private CustomResponse nonExistentUserById(){
        return CustomResponse.builder()
                    .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .responseBody(null)
                    .info(null)
                    .build();

    }
}
