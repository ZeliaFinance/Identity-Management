package com.zeliafinance.identitymanagement.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.zeliafinance.identitymanagement.config.JwtTokenProvider;
import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.Role;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request.CreateCustomerDto;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response.CreateCustomerResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.bani.service.BaniService;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request.NinRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response.NinLookupResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.service.DojahSmsService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


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
    private DojahSmsService dojahSmsService;
    private AmazonS3 amazonS3;
    private BaniService baniService;

    public ResponseEntity<CustomResponse> signUp(SignUpRequest request){
        boolean isEmailExist = userCredentialRepository.existsByEmail(request.getEmail());
        if (isEmailExist){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage(AccountUtils.EMAIL_EXISTS_MESSAGE)
                    .build());
        }

        if (!request.getPassword().equals(request.getConfirmPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseBody(AccountUtils.PASSWORD_INCORRECT_MESSAGE)
                    .build());
        }

        if (!accountUtils.isPasswordValid(request.getPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.PASSWORD_INVALID_MESSAGE)
                    .build());
        }

        UserCredential userCredential = UserCredential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .walletId(accountUtils.generateAccountNumber())
                .emailVerifyStatus("UNVERIFIED")
                .referralCode(accountUtils.generateReferralCode())
                .referredBy(request.getReferredBy())
                .hashedPassword(accountUtils.encode(request.getPassword(), 3))
                .accountStatus("PENDING")
                .accountBalance(0)
                .availableBalance(0)
                .inviteAccepted(false)
                .build();

        userCredential.setRole(Role.ROLE_USER);
        UserCredential savedUser = userCredentialRepository.save(userCredential);


        CustomResponse otpResponse = sendOtp(OtpDto.builder()
                .email(request.getEmail())
                .build()).getBody();

        assert otpResponse != null;
        String otp = otpResponse.getReferenceId().substring(0, 6);
        String referenceId = otpResponse.getReferenceId().substring(6);
        LocalDateTime expiryDate = otpResponse.getExpiry();

        savedUser.setOtp(otp);
        savedUser.setReferenceId(referenceId);
        savedUser.setOtpExpiryDate(expiryDate);

        userCredentialRepository.save(savedUser);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(request.getEmail())
                .subject(AccountUtils.ACCOUNT_CONFIRMATION_SUBJECT)
                .messageBody(AccountUtils.ACCOUNT_CONFIRMATION_MESSAGE)
                .build();
        emailService.sendEmailAlert(emailDetails);

        String token = generateToken(request.getEmail());


        Object response = modelMapper.map(savedUser, UserCredentialResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .responseBody(response)
                        .referenceId(userCredential.getReferenceId())
                        .expiry(userCredential.getOtpExpiryDate())
                        .token(token)
                .build());
    }



    public ResponseEntity<CustomResponse> updateUserProfile(UserProfileRequest request){
        String email = getLoggedInUserEmail();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        boolean isUserExist = userCredentialRepository.existsByEmail(email);
        LocalDate currentDate = LocalDate.now();

        if (request.getDateOfBirth() != null){
            long age = ChronoUnit.YEARS.between(request.getDateOfBirth(), currentDate);
            log.info("age {}", age);
            if (age < 18){
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                        .responseMessage(AccountUtils.UNDERAGE_MESSAGE)
                        .build());
            }
        }

        if (userCredential.getEmailVerifyStatus().equalsIgnoreCase("UNVERIFIED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.EMAIL_NOT_VERIFIED_MESSAGE)
                    .build());
        }

        if (isUserExist){
            if (request.getFirstName() != null){
                userCredential.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null){
                userCredential.setLastName(request.getLastName());
            }
            if (request.getOtherName() != null){
                userCredential.setOtherName(request.getOtherName());
            }
            if (request.getDateOfBirth() != null){
                userCredential.setDateOBirth(request.getDateOfBirth());
            }
            if (request.getPhoneNumber() != null){
                boolean isPhoneNumberExists = userCredentialRepository.existsByPhoneNumber(request.getPhoneNumber());
                if (isPhoneNumberExists){
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .responseMessage(AccountUtils.NON_UNIQUE_PHONE_NUMBER_MESSAGE)
                            .build());
                }
                userCredential.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getGender() != null){
                userCredential.setGender(request.getGender());
            }
            if (request.getMaritalStatus() != null){
                userCredential.setMaritalStatus(request.getMaritalStatus());
            }
            if (request.getAddress() != null){
                userCredential.setAddress(request.getAddress());
            }
            int level;
            if (userCredential.getProfileSetupLevel() == null){
                level = 1;
                userCredential.setProfileSetupLevel(level);
            }
            UserCredential updatedUser = userCredentialRepository.save(userCredential);
            //Sending email alert

            //building response object
            Object response = modelMapper.map(updatedUser, UserCredentialResponse.class);
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                    .responseMessage(AccountUtils.PROFILE_UPDATE_SUCCESS)
                    .responseBody(response)
                    .build());

        }

        return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                .responseBody(null)
                .build());
    }

    public ResponseEntity<CustomResponse> updateUserBvn(UserProfileRequest request){
        String email = getLoggedInUserEmail();
        boolean isUserExist = userCredentialRepository.existsByEmail(email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();

        if (userCredential.getEmailVerifyStatus().equalsIgnoreCase("UNVERIFIED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                    .responseMessage(AccountUtils.EMAIL_NOT_VERIFIED_MESSAGE)
                    .build());
        }

        if (isUserExist){
            if (request.getBvn() != null){

                boolean isBvnExist = userCredentialRepository.existsByBvn(request.getBvn());
                if (isBvnExist){
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .responseMessage(AccountUtils.NON_UNIQUE_BVN_MESSAGE)
                            .build());
                }

                userCredential.setBvn(request.getBvn());

                if (request.getBvn().length() != AccountUtils.BVN_LENGTH){
                    userCredential.setBvnVerifyStatus("UNVERIFIED");
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.BVN_INVALID_MESSAGE)
                            .build());
                }

                if ((!request.getBvn().startsWith("1234") && !request.getBvn().endsWith("02"))){
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.BVN_INVALID_MESSAGE)
                            .build());
                }

                userCredential.setBvnVerifyStatus("VERIFIED");
                userCredentialRepository.save(userCredential);
            }

            }
            log.info("Bvn status: {}", userCredential.getBvnVerifyStatus());
            if (userCredential.getProfileSetupLevel() < 2 && userCredential.getBvnVerifyStatus().equalsIgnoreCase("VERIFIED")){
                userCredential.setProfileSetupLevel(2);
            }

            UserCredential updatedUser = userCredentialRepository.save(userCredential);
            //Sending email alert

            //building response object
            Object response = modelMapper.map(updatedUser, UserCredentialResponse.class);
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(response)
                    .build());


    }

    public ResponseEntity<CustomResponse> updateUserNin(UserProfileRequest request){
        String email = getLoggedInUserEmail();
        boolean isUserExist = userCredentialRepository.existsByEmail(email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();

        if (userCredential.getEmailVerifyStatus().equalsIgnoreCase("UNVERIFIED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage(AccountUtils.EMAIL_NOT_VERIFIED_MESSAGE)
                    .build());
        }

        if (isUserExist){
            if (request.getNin() != null){
                if (!accountUtils.validateBvnAndNin(request.getNin())){
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.NIN_INVALID_MESSAGE)
                            .build());
                }
                userCredential.setNin(request.getNin());
                if (request.getNin().length() != AccountUtils.BVN_LENGTH){
                    userCredential.setNinStatus("UNVERIFIED");
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.NIN_INVALID_MESSAGE)
                            .build());
                }

                if ((!request.getNin().startsWith("1234") && !request.getNin().endsWith("02"))){
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.NIN_INVALID_MESSAGE)
                            .build());
                }

                userCredential.setNinStatus("VERIFIED");
                userCredentialRepository.save(userCredential);
            }
            if (userCredential.getProfileSetupLevel() < 3 && userCredential.getNinStatus().equalsIgnoreCase("VERIFIED")){
                userCredential.setProfileSetupLevel(3);
            }

            UserCredential updatedUser = userCredentialRepository.save(userCredential);
            //Sending email alert

            //building response object
            Object response = modelMapper.map(updatedUser, UserCredentialResponse.class);
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(response)
                    .build());

        }

        return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                .responseBody(null)
                .build());
    }

    public ResponseEntity<CustomResponse> updateUserSecurityQuestion(UserProfileRequest request){
        String email = getLoggedInUserEmail();
        boolean isUserExist = userCredentialRepository.existsByEmail(email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();

        if (userCredential.getEmailVerifyStatus().equalsIgnoreCase("UNVERIFIED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage(AccountUtils.EMAIL_NOT_VERIFIED_MESSAGE)
                    .build());
        }

        if (isUserExist){
            if (request.getSecurityQuestion() != null){
                userCredential.setSecurityQuestion(request.getSecurityQuestion());
                userCredential.setSecurityAnswer(request.getSecurityAnswer());
            }
            if (userCredential.getProfileSetupLevel() < 5){
                userCredential.setProfileSetupLevel(5);
            }
            log.info("phone number: {}", userCredential.getPhoneNumber());

            String prefix = "+234";
            String suffix = userCredential.getPhoneNumber().substring(1);
            String phoneNumber = prefix + suffix;
            log.info("Phone Number: {}", phoneNumber);
            CreateCustomerResponse baniCustomer = baniService.createCustomer(CreateCustomerDto.builder()
                            .customer_first_name(userCredential.getFirstName())
                            .customer_last_name(userCredential.getLastName())
                            .customer_phone(phoneNumber)
                            .customer_email(userCredential.getEmail())
                            .customer_address(userCredential.getAddress())
                            .customer_city("Zelia")
                            .customer_state("Zelia")
                            .customer_note("Zelia")
                    .build());

            String customerRef = baniCustomer.getCustomerRef();
            userCredential.setCustomerRef(customerRef);
            userCredential.setAccountStatus("Active");

            UserCredential updatedUser = userCredentialRepository.save(userCredential);
            //Sending email alert

            //building response object
            Object response = modelMapper.map(updatedUser, UserCredentialResponse.class);
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(response)
                    .build());

        }

        return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                .responseBody(null)
                .build());
    }


    public ResponseEntity<CustomResponse> login(LoginDto loginDto){
        Authentication authentication=null;
        UserCredential userCredential = userCredentialRepository.findByEmail(loginDto.getEmail()).get();
        if (loginDto.getAuthMethod().equalsIgnoreCase("biometric")){
            log.info("using biometric login");
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), accountUtils.decode(userCredential.getHashedPassword(), 3))
            );


        } else if(loginDto.getAuthMethod().equalsIgnoreCase("nonBiometric")){
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );
        }

        assert authentication != null;
        if (!authentication.isAuthenticated()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.INVALID_CREDENTIALS_MESSAGE)
                    .build());
        }

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
        userCredential.setLastLoggedIn(LocalDateTime.now());
        userCredentialRepository.save(userCredential);


        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                .responseMessage(AccountUtils.LOGIN_SUCCESS_MESSAGE)
                .token(jwtTokenProvider.generateToken(authentication))
                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public ResponseEntity<CustomResponse> adminLogin(LoginDto loginDto){
        Authentication authentication;
        UserCredential userCredential = userCredentialRepository.findByEmail(loginDto.getEmail()).get();
        if (!(userCredential.getRole().equals(Role.ROLE_ADMIN) || userCredential.getRole().equals(Role.ROLE_SUPER_ADMIN))){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.NON_ADMIN_LOGIN)
                    .build());
        }
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
                        loginDto.getPassword()));

        if (!authentication.isAuthenticated()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage(AccountUtils.INVALID_CREDENTIALS_MESSAGE)
                    .build());
        }

        EmailDetails loginAlert = EmailDetails.builder()
                .subject("YOU'RE LOGGED IN!")
                .recipient(loginDto.getEmail())
                .messageBody("You logged into your account!!!")
                .build();

        emailService.sendEmailAlert(loginAlert);



        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .responseMessage(AccountUtils.LOGIN_SUCCESS_MESSAGE)
                .token(jwtTokenProvider.generateToken(authentication))
                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }


    public ResponseEntity<CustomResponse> fetchAllUsers(int pageNo, int pageSize) {

        List<UserCredentialResponse> usersList = userCredentialRepository.findAll()
                .stream()
                .skip(pageNo-1)
                .limit(pageSize).sorted(Comparator.comparing(UserCredential::getCreatedAt).reversed())
                .map(userCredential -> modelMapper.map(userCredential, UserCredentialResponse.class))
                .toList();


        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .responseMessage("SUCCESS")
                .responseBody(usersList)
                .info(Info.builder()
                        .totalPages((usersList.size()/pageSize)+1)
                        .totalElements((long)usersList.size())
                        .pageSize(pageSize)
                        .build())
                .build());

    }

    public ResponseEntity<CustomResponse> fetchUser(Long userId){
        boolean isUserExists = userCredentialRepository.existsById(userId);
        if (!isUserExists){
            nonExistentUserById();
        }
        UserCredential userCredential = userCredentialRepository.findById(userId).orElseThrow();
        UserCredentialResponse response = modelMapper.map(userCredential, UserCredentialResponse.class);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .responseBody(response)
                .build());
    }

    public ResponseEntity<CustomResponse> updateUserRole(Long userId){
        boolean isUserExist = userCredentialRepository.existsById(userId);
        if (!isUserExist){
            nonExistentUserById();
        }
        UserCredential userCredential = userCredentialRepository.findById(userId).orElseThrow();
        userCredential.setRole(Role.ROLE_ADMIN);

        userCredentialRepository.save(userCredential);
        Object response = modelMapper.map(userCredential, UserCredentialResponse.class);
        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .responseBody(AccountUtils.USER_ROLE_SET_MESSAGE)
                .responseBody(response)
                .info(null)
                .build());

    }

    public ResponseEntity<CustomResponse> resetPassword(String email){
        UserCredential userCredential = userCredentialRepository.findByEmail(email).orElseThrow();
        boolean isUserExists = userCredentialRepository.existsByEmail(email);
        if (!isUserExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        if(userCredential.getRole().equals(Role.ROLE_SUPER_ADMIN)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .responseMessage("Super Admin Cannot Reset Password. Contact the Engineering team for help")
                    .build());
        }

        CustomResponse otpResponse = sendOtp(OtpDto.builder()
                .email(email)
                .build()).getBody();

        assert otpResponse != null;
        String otp = otpResponse.getReferenceId().substring(0, 6);
        String referenceId = otpResponse.getReferenceId().substring(6);
        LocalDateTime expiryDate = otpResponse.getExpiry();

        userCredential.setOtp(otp);
        userCredential.setReferenceId(referenceId);
        userCredential.setOtpExpiryDate(expiryDate);

        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.PASSWORD_RESET_MESSAGE)
                        .responseBody(otpResponse)
                .build());
    }

    public ResponseEntity<CustomResponse> changePassword(String email, PasswordResetDto passwordResetDto){
        boolean isUserExists = userCredentialRepository.existsByEmail(email);
        if (!isUserExists){
            nonExistentUserById();
        }

        if (!passwordResetDto.getNewPassword().equals(passwordResetDto.getConfirmNewPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.PASSWORD_INCORRECT_MESSAGE)
                    .build());
        }
        if (!accountUtils.isPasswordValid(passwordResetDto.getNewPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.PASSWORD_INVALID_MESSAGE)
                    .build());
        }
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        if(userCredential.getRole().equals(Role.ROLE_SUPER_ADMIN)){
            String message = "Super Admin Cannot Reset Password. Contact the Engineering team for help";
            log.info("Error message: {}", message);
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.FORBIDDEN.value())
                    .responseMessage(message)
                    .build());
        }
        CustomResponse validationResponse = validateOtp(ValidateOtpDto.builder()
                .email(email)
                .otp(passwordResetDto.getOtp())
                .build()).getBody();

        assert validationResponse != null;
        if (!validationResponse.getOtpStatus()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.INVALID_OTP_MESSAGE)
                    .build());
        }

        userCredential.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
        userCredential.setHashedPassword(accountUtils.encode(passwordResetDto.getNewPassword(), 3));
        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.PASSWORD_RESET_SUCCESS_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> generateReferralLink(String email){
        String baseUrl = "http://localhost:5000/";
        boolean isUserExists = userCredentialRepository.existsByEmail(email);
        if (!isUserExists){
            return ResponseEntity.internalServerError()
                    .body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                            .build());
        }
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(baseUrl + userCredential.getReferralCode())
                .build());
    }

    public ResponseEntity<CustomResponse> verifyCustomerIdentity(NinVerificationDto ninDto){
        Optional<UserCredential> userCredential = userCredentialRepository.findByEmail(ninDto.getEmail());
        if (userCredential.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        String fullNameOnRequest = userCredential.get().getFirstName() + userCredential.get().getLastName() + userCredential.get().getOtherName();
        char[] fullNameOnRequestArray = fullNameOnRequest.toCharArray();
        Arrays.sort(fullNameOnRequestArray);
        LocalDate dateOfBirthOnRequest = userCredential.get().getDateOBirth();

        NinLookupResponse ninResponse = dojahSmsService.ninLookup(NinRequest.builder()
                        .nin(ninDto.getNin())
                .build());

        String fullNameOnNin = ninResponse.getEntity().getSurname() + ninResponse.getEntity().getMiddlename() + ninResponse.getEntity().getFirstname();
        char[] fullNameOnNinArray = fullNameOnNin.toCharArray();
        Arrays.sort(fullNameOnNinArray);
        LocalDate dateOfBirthOnNin = LocalDate.parse(ninResponse.getEntity().getBirthdate());

        log.info("dob on nin {}", dateOfBirthOnNin);
        if (Arrays.equals(fullNameOnRequestArray, fullNameOnNinArray) && dateOfBirthOnNin.equals(dateOfBirthOnRequest)){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .responseMessage(AccountUtils.IDENTITY_VERIFY_SUCCESS_MESSAGE)
                            .responseBody("VERIFIED")

                    .build());
        }

        return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .responseMessage(AccountUtils.IDENTITY_VERIFICATION_FAIL_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> verifyBvn(BvnVerificationDto bvnVerificationDto){
        UserCredential userCredential = userCredentialRepository.findByEmail(bvnVerificationDto.getEmail()).get();
        boolean existsByEmail = userCredentialRepository.existsByEmail(bvnVerificationDto.getEmail());
        if (existsByEmail){

            if (bvnVerificationDto.getBvn().length() != AccountUtils.BVN_LENGTH){
                userCredential.setBvnVerifyStatus("UNVERIFIED");
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .responseMessage(AccountUtils.BVN_INVALID_MESSAGE)
                        .build());
            }

            if ((!bvnVerificationDto.getBvn().startsWith("1234") && !bvnVerificationDto.getBvn().endsWith("02"))){
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(400)
                        .responseMessage(AccountUtils.BVN_INVALID_MESSAGE)
                        .build());
            }

            userCredential.setBvnVerifyStatus("VERIFIED");
        }
        userCredential = userCredentialRepository.save(userCredential);
        return ResponseEntity.badRequest().body(CustomResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public ResponseEntity<CustomResponse> verifyNin(NinVerificationDto ninVerificationDto){
        UserCredential userCredential = userCredentialRepository.findByEmail(ninVerificationDto.getEmail()).get();
        boolean existsByEmail = userCredentialRepository.existsByEmail(ninVerificationDto.getEmail());
        if (existsByEmail){
            if (ninVerificationDto.getNin().length() != AccountUtils.BVN_LENGTH){
                userCredential.setNinStatus("UNVERIFIED");
                userCredentialRepository.save(userCredential);
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .responseMessage(AccountUtils.NIN_INVALID_MESSAGE)
                        .build());
            }

//            if (!ninVerificationDto.getNin().equals("70123456789")){
//                userCredential.setNinStatus("UNVERIFIED");
//                userCredentialRepository.save(userCredential);
//                return ResponseEntity.badRequest().body(CustomResponse.builder()
//                        .statusCode(HttpStatus.BAD_REQUEST.value())
//                        .responseMessage(AccountUtils.NIN_MISMATCH_MESSAGE)
//                        .build());
//            }

            userCredential.setNinStatus("VERIFIED");
        }
        userCredential = userCredentialRepository.save(userCredential);
        return ResponseEntity.ok().body(CustomResponse.builder()
                .statusCode(200)
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public ResponseEntity<CustomResponse> loggedInUser(String email){
        boolean isEmailExists = userCredentialRepository.existsByEmail(email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();

        if (!isEmailExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }


    private CustomResponse nonExistentUserById(){
        return CustomResponse.builder()
                    .statusCode(HttpStatus.OK.value())
                    .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .responseBody(null)
                    .info(null)
                    .build();

    }

    public ResponseEntity<CustomResponse> saveBiometricInfo(LoginDto loginDto){
        boolean isEmailExists = userCredentialRepository.existsByEmail(loginDto.getEmail());
        if (!isEmailExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        UserCredential userCredential = userCredentialRepository.findByEmail(loginDto.getEmail()).get();

        userCredential.setHashedPassword(accountUtils.encode(loginDto.getPassword(), 3));
        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.BIOMETRIC_INFO_SAVED_MESSAGE)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());

    }

    public ResponseEntity<CustomResponse> sendOtp(OtpDto request){
        String otp = accountUtils.generateOtp();
        String referenceId = otp + UUID.randomUUID();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        boolean isEmailExist = userCredentialRepository.existsByEmail(request.getEmail());
        UserCredential userCredential = userCredentialRepository.findByEmail(request.getEmail()).get();

        if (!isEmailExist){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        userCredential.setOtp(otp);
        userCredential.setReferenceId(referenceId);
        userCredential.setOtpExpiryDate(expiryDate);

        userCredentialRepository.save(userCredential);


        emailService.sendEmailAlert(EmailDetails.builder()
                        .messageBody("Your pending Otp from zelia is " + otp + ". Valid for 8 minutes. PLEASE DO NOT DISCLOSE!")
                        .subject("ZELIAFINANCE")
                        .recipient(request.getEmail())
                .build());

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.OTP_SENT_MESSAGE)
                        .referenceId(referenceId)
                        .expiry(expiryDate)
                .build());
    }

    public ResponseEntity<CustomResponse> validateOtp(ValidateOtpDto request){
        UserCredential userCredential = userCredentialRepository.findByEmail(request.getEmail()).orElseThrow();
        if (LocalDateTime.now().isAfter(userCredential.getOtpExpiryDate())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.OTP_EXPIRED_MESSAGE)
                            .otpStatus(false)
                    .build());
        }

        log.info("otp expiry date : {}", userCredential.getOtpExpiryDate());

        if (!request.getOtp().equals(userCredential.getOtp())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseMessage(AccountUtils.INVALID_OTP_MESSAGE)
                            .otpStatus(false)
                    .build());
        }
        String token = generateToken(request.getEmail());
        userCredential.setEmailVerifyStatus("VERIFIED");
        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.OTP_VALIDATED_MESSAGE)
                        .otpStatus(true)
                        .token(token)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());

    }

    public ResponseEntity<CustomResponse> pinSetup(PinSetupDto request){
        UserCredential userCredential = userCredentialRepository.findByEmail(request.getEmail()).get();
        boolean isEmailExists = userCredentialRepository.existsByEmail(request.getEmail());
        if (!userCredential.getEmailVerifyStatus().equalsIgnoreCase("VERIFIED")){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.EMAIL_NOT_VERIFIED_MESSAGE)
                    .build());
        }
        if (!isEmailExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }
        boolean isPinValid = accountUtils.isPinValid(request.getPin(), userCredential.getDateOBirth().getYear());
        if (!isPinValid){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.INVALID_PIN_MESSAGE)
                    .build());
        }

        if (!request.getPin().equals(request.getConfirmPin())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.PIN_DISPARITY_MESSAGE)
                    .build());
        }

        userCredential.setPin(accountUtils.encodePin(request.getPin()));
        if (userCredential.getProfileSetupLevel() < 4){
            userCredential.setProfileSetupLevel(4);
        }

        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.PIN_SETUP_SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public ResponseEntity<CustomResponse> verifyPin(PinSetupDto pinSetupDto){
        UserCredential userCredential = userCredentialRepository.findByEmail(pinSetupDto.getEmail()).orElseThrow();
        String savedPin = accountUtils.decodePin(userCredential.getPin());
        log.info(savedPin);
        if (userCredential.getAccountStatus().equalsIgnoreCase("LOCKED")){
            return ResponseEntity.status(HttpStatus.LOCKED).body(CustomResponse.builder()
                            .statusCode(HttpStatus.LOCKED.value())
                            .responseMessage("Your account is locked. Please try again in 5 minutes")
                            .pinVerificationStatus(false)
                    .build());
        }
        if (!pinSetupDto.getPin().equals(pinSetupDto.getConfirmPin())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.PIN_DISPARITY_MESSAGE)
                            .pinVerificationStatus(false)
                    .build());
        }

        if (!savedPin.equals(pinSetupDto.getPin())){
            handleIncorrectPinAttempt(userCredential);
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage(AccountUtils.INVALID_PIN_MESSAGE)
                    .pinVerificationStatus(false)
                    .build());
        }

        resetIncorrectPinAttempts(userCredential);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.PIN_VALIDATED_MESSAGE)
                        .pinVerificationStatus(true)
                .build());
    }

    private void handleIncorrectPinAttempt(UserCredential userCredential){
        int maximumAttempts = 3;
        int lockoutDurationInMinutes = 5;
        userCredential.setFailedPinAttempts(userCredential.getFailedPinAttempts() + 1);
        if (userCredential.getFailedPinAttempts() >= maximumAttempts){
            userCredential.setAccountStatus("LOCKED");
            userCredential.setLockoutTimeStamp(LocalDateTime.now().plusMinutes(lockoutDurationInMinutes));
        }
    }

    private void resetIncorrectPinAttempts(UserCredential userCredential){
        userCredential.setFailedPinAttempts(0);
        userCredential.setAccountStatus(null);
    }

    private String generateToken(String subject){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + AccountUtils.JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key())
                .compact();
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(AccountUtils.JWT_SECRET));
    }


    public ResponseEntity<CustomResponse> uploadFile(final Optional<MultipartFile> multipartFile, Long userId){
        UserCredential userCredential = userCredentialRepository.findById(userId).get();
        boolean existsById = userCredentialRepository.existsById(userId);
        if (!existsById){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }


        log.info("File upload initiated.");


        try {
            final File file = convertMultiPartFileToFile(multipartFile);
            String fileName = uploadFileToS3Bucket(file);
            log.info("File upload is completed.");
            userCredential.setImageFileName(fileName);
            userCredential.setImagePath(file.getPath());
            userCredentialRepository.save(userCredential);
            file.delete();
        } catch (final AmazonServiceException e) {
            log.info("File upload failed.");
            log.error("Error = {} while uploading file.", e.getMessage());
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public File convertMultiPartFileToFile(final Optional<MultipartFile> multipartFile){
        if (multipartFile.isPresent()){
            final File file = new File(Objects.requireNonNull(multipartFile.get().getOriginalFilename()));
            try (final FileOutputStream outputStream = new FileOutputStream(file)){
                outputStream.write(multipartFile.get().getBytes());
            } catch (IOException e) {
                log.error("Error converting multipart file to file= {}", e.getMessage());
                throw new RuntimeException(e);
            }
            return file;
        }

        return null;
    }

    public String uploadFileToS3Bucket(final File file){
        final String uniqueFileName = LocalDateTime.now() + "_" + file.getName();
        log.info("Uploading file with name= " + uniqueFileName);
        final PutObjectRequest putObjectRequest = new PutObjectRequest(AccountUtils.BUCKET_NAME, uniqueFileName, file);
        amazonS3.putObject(putObjectRequest);
        return uniqueFileName;
    }

    private String getLoggedInUserEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public ResponseEntity<CustomResponse> findUserByEmail(String email) {
        //check if user with the email exists
        //if not, return an error message
        //return customer with user object

        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        boolean isEmailExist = userCredentialRepository.existsByEmail(email);
        if (!isEmailExist) {
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(200)
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .responseBody(modelMapper.map(userCredential, UserProfileRequest.class))
                .build());
    }

    public ResponseEntity<List<UserProfileRequest>> searchUsersByKey(String key) {
        List<UserCredential> userCredentials = userCredentialRepository.searchUsersByKey(key);
        List<UserProfileRequest> profileRequestList = userCredentials.stream().map(userCredential -> modelMapper.map(userCredential, UserProfileRequest.class)).toList();

        if ((userCredentials.isEmpty())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(profileRequestList);
        }
    }

    public ResponseEntity<CustomResponse> getTotalUsers() {
        int totalUsers = userCredentialRepository.findAll().size();
        int verifiedUsers = userCredentialRepository.findByEmailVerifyStatusEquals("VERIFIED").size();
        int nonVerifiedUsers = userCredentialRepository.findByEmailVerifyStatusEquals("UNVERIFIED").size();
        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("Total Users", totalUsers);
        dataMap.put("Verified Users", verifiedUsers);
        dataMap.put("Non Verified Users", nonVerifiedUsers);
        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(200)
                .responseBody(dataMap)
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> validateToken(ValidateTokenRequest request){
        boolean isTokenValid = jwtTokenProvider.validateToken(request.getToken());
        if (!isTokenValid){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.INVALID_TOKEN_MESSAGE)
                    .build());
        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .token(request.getToken())
                        .responseBody(true)
                .build());
    }

    public ResponseEntity<CustomResponse> logout(HttpServletRequest request, HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null){
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .build());
    }
}
