package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.config.JwtTokenProvider;
import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.Role;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.service.DojahSmsService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
    private DojahSmsService dojahSmsService;




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

        if (!accountUtils.isPasswordValid(request.getPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.PASSWORD_INVALID_CODE)
                            .responseMessage(AccountUtils.PASSWORD_INVALID_MESSAGE)
                    .build());
        }

        CustomResponse otpResponse = sendOtp(OtpDto.builder()
                .email(request.getEmail())
                .build()).getBody();

        assert otpResponse != null;
        String otp = otpResponse.getReferenceId().substring(0, 6);
        String referenceId = otpResponse.getReferenceId().substring(6);
        LocalDateTime expiryDate = otpResponse.getExpiry();


        UserCredential userCredential = UserCredential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .walletId(accountUtils.generateAccountNumber())
                .emailVerifyStatus("UNVERIFIED")
                .referralCode(accountUtils.generateReferralCode())
                .referredBy(request.getReferredBy())
                .hashedPassword(request.getPassword())
                .otp(otp)
                .referenceId(referenceId)
                .otpExpiryDate(expiryDate)
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
                        .referenceId(userCredential.getReferenceId())
                        .expiry(userCredential.getOtpExpiryDate())
                .build());
    }



    public ResponseEntity<CustomResponse> updateUserProfile(Long userId, UserProfileRequest request){
        boolean isUserExist = userCredentialRepository.existsById(userId);
        UserCredential userCredential = userCredentialRepository.findById(userId).get();
        LocalDate currentDate = LocalDate.now();

        if (request.getDateOfBirth() != null){
            long age = ChronoUnit.YEARS.between(request.getDateOfBirth(), currentDate);
            log.info("age {}", age);
            if (age < 18){
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .responseCode(AccountUtils.UNDERAGE_CODE)
                        .responseMessage(AccountUtils.UNDERAGE_MESSAGE)
                        .build());
            }
        }


        if (isUserExist){
            userCredential.setFirstName(request.getFirstName());
            userCredential.setLastName(request.getLastName());
            userCredential.setOtherName(request.getOtherName());
            userCredential.setDateOBirth(request.getDateOfBirth());
            userCredential.setPhoneNumber(request.getPhoneNumber());
            userCredential.setMobileNumber(request.getMobileNumber());
            userCredential.setWhatsAppNumber(request.getWhatsAppNumber());
            userCredential.setGender(request.getGender());
            userCredential.setBvn(request.getBvn());
            userCredential.setBvnVerifyStatus(userCredential.getBvnVerifyStatus());
            userCredential.setNin(request.getNin());
            userCredential.setNinStatus(userCredential.getNinStatus());
            String pin=userCredential.getPin();
            if (!request.getPin().equals("")){
                pin = request.getPin();
                if (!accountUtils.isPinValid(pin, request.getDateOfBirth().getYear())){
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.INVALID_PIN_CODE)
                            .responseMessage(AccountUtils.INVALID_PIN_MESSAGE)
                            .build());
                }

                if (!pin.equals(request.getConfirmPin())){
                    return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.PIN_DISPARITY_CODE)
                            .responseMessage(AccountUtils.PIN_DISPARITY_MESSAGE)
                            .build());
                }
            }
            userCredential.setPin(pin);

            userCredential.setAccountStatus("PENDING");

            userCredential.setDeviceIp(request.getDeviceIp());
            userCredential.setLiveLocation(request.getLiveLocation());
            userCredential.setModifiedby(SecurityContextHolder.getContext().getAuthentication().getName());
            userCredential.setReferredBy(request.getReferredBy());

            UserCredential updatedUser = userCredentialRepository.save(userCredential);

            //Sending email alert

            //building response object
            Object response = modelMapper.map(updatedUser, UserCredentialResponse.class);
            return ResponseEntity.ok(CustomResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                    .responseBody(response)
                    .build());

        }

        return ResponseEntity.badRequest().body(CustomResponse.builder()
                .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
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



        return ResponseEntity.ok(CustomResponse.builder()
                .responseCode(AccountUtils.LOGIN_SUCCESS_CODE)
                .responseMessage(AccountUtils.LOGIN_SUCCESS_MESSAGE)
                .token(jwtTokenProvider.generateToken(authentication))
                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public ResponseEntity<CustomResponse> fetchAllUsers(int pageNo, int pageSize){

        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<UserCredential> userCredentials = userCredentialRepository.findAll(pageable);
        List<UserCredential> list = userCredentials.getContent();

        Object response = list.stream().map(user -> modelMapper.map(user, UserCredentialResponse.class)).collect(Collectors.toList());


        return ResponseEntity.ok(CustomResponse.builder()
                .responseCode(String.valueOf(HttpStatus.OK))
                .responseMessage("SUCCESS")
                .responseBody(response)
                .info(Info.builder()
                        .totalPages(userCredentials.getTotalPages())
                        .totalElements(userCredentials.getTotalElements())
                        .pageSize(userCredentials.getSize())
                        .build())

                .build());

    }


    public ResponseEntity<CustomResponse> fetchUser(Long userId){
        boolean isUserExists = userCredentialRepository.existsById(userId);
        if (!isUserExists){
            nonExistentUserById();
        }
        UserCredential userCredential = userCredentialRepository.findById(userId).orElseThrow();
        Object response = modelMapper.map(userCredential, UserCredentialResponse.class);
        return ResponseEntity.ok(CustomResponse.builder()
                .responseCode(String.valueOf(HttpStatus.valueOf("OK")))
                .responseMessage(HttpStatus.OK.name())
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
                .responseCode(AccountUtils.USER_ROLE_SET_CODE)
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
                            .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        CustomResponse otpResponse = sendOtp(OtpDto.builder()
                .email(email)
                .build()).getBody();

        String otp = otpResponse.getReferenceId().substring(0, 6);
        String referenceId = otpResponse.getReferenceId().substring(6);
        LocalDateTime expiryDate = otpResponse.getExpiry();

        userCredential.setOtp(otp);
        userCredential.setReferenceId(referenceId);
        userCredential.setOtpExpiryDate(expiryDate);

        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.PASSWORD_RESET_CODE)
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
                            .responseCode(AccountUtils.PASSWORD_INCORRECT_CODE)
                            .responseMessage(AccountUtils.PASSWORD_INCORRECT_MESSAGE)
                    .build());
        }
        if (!accountUtils.isPasswordValid(passwordResetDto.getNewPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.PASSWORD_INVALID_CODE)
                            .responseMessage(AccountUtils.PASSWORD_INVALID_MESSAGE)
                    .build());
        }
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        CustomResponse validationResponse = validateOtp(ValidateOtpDto.builder()
                .email(email)
                .otp(passwordResetDto.getOtp())
                .build()).getBody();

        if (!validationResponse.getOtpStatus()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.INVALID_OTP_CODE)
                            .responseMessage(AccountUtils.INVALID_OTP_MESSAGE)
                    .build());
        }

        userCredential.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
        userCredential.setHashedPassword(accountUtils.encode(passwordResetDto.getNewPassword(), 3));
        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.PASSWORD_RESET_SUCCESS_CODE)
                        .responseMessage(AccountUtils.PASSWORD_RESET_SUCCESS_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> generateReferralLink(String email){
        String baseUrl = "http://localhost:5000/";
        boolean isUserExists = userCredentialRepository.existsByEmail(email);
        if (!isUserExists){
            return ResponseEntity.internalServerError()
                    .body(CustomResponse.builder()
                            .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                            .build());
        }
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(HttpStatus.OK.toString())
                        .responseMessage(baseUrl + userCredential.getReferralCode())
                .build());
    }

    public ResponseEntity<CustomResponse> verifyCustomerIdentity(NinVerificationDto ninDto){
        Optional<UserCredential> userCredential = userCredentialRepository.findByEmail(ninDto.getEmail());
        if (userCredential.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
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
                            .responseCode(AccountUtils.IDENTITY_VERIFY_SUCCESS_CODE)
                            .responseMessage(AccountUtils.IDENTITY_VERIFY_SUCCESS_MESSAGE)
                            .responseBody("VERIFIED")

                    .build());
        }

        return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .responseCode(AccountUtils.IDENTITY_VERIFICATION_FAIL_CODE)
                        .responseMessage(AccountUtils.IDENTITY_VERIFICATION_FAIL_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> verifyBvn(BvnVerificationDto bvnVerificationDto){
        UserCredential userCredential = userCredentialRepository.findByEmail(bvnVerificationDto.getEmail()).get();
        boolean existsByEmail = userCredentialRepository.existsByEmail(bvnVerificationDto.getEmail());
        if (existsByEmail){
            DojahBvnResponse bvnResponse = dojahSmsService.basicBvnLookUp(BvnRequest.builder()
                    .bvn(bvnVerificationDto.getBvn())
                    .build());
            String bvnFullName = bvnResponse.getEntity().getFirstName() + bvnResponse.getEntity().getLastName() + bvnResponse.getEntity().getMiddleName();
            String requestName = userCredential.getFirstName() + userCredential.getLastName() + userCredential.getOtherName();
            char[] bvnFullNameArray = bvnFullName.toCharArray();
            char[] requestNameArray = requestName.toCharArray();
            Arrays.sort(bvnFullNameArray);
            Arrays.sort(requestNameArray);

            if (Arrays.equals(bvnFullNameArray, requestNameArray)){
                userCredential.setBvnVerifyStatus("VERIFIED");
                userCredential = userCredentialRepository.save(userCredential);
                return ResponseEntity.ok(CustomResponse.builder()
                                .responseCode(AccountUtils.BVN_VALID_CODE)
                                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                                .responseMessage(AccountUtils.BVN_VALID_MESSAGE)
                        .build());
            }
        }
        userCredential.setBvnVerifyStatus("UNVERIFIED");
        userCredential = userCredentialRepository.save(userCredential);
        return ResponseEntity.badRequest().body(CustomResponse.builder()
                .responseCode(AccountUtils.BVN_INVALID_CODE)
                .responseMessage(AccountUtils.BVN_INVALID_MESSAGE)
                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public ResponseEntity<CustomResponse> verifyNin(NinVerificationDto ninVerificationDto){
        UserCredential userCredential = userCredentialRepository.findByEmail(ninVerificationDto.getEmail()).get();
        boolean existsByEmail = userCredentialRepository.existsByEmail(ninVerificationDto.getEmail());
        if (existsByEmail){
            NinLookupResponse ninLookupResponse =dojahSmsService.ninLookup(NinRequest.builder()
                            .nin(ninVerificationDto.getNin())
                    .build());

            String ninFullName = ninLookupResponse.getEntity().getFirstname() + ninLookupResponse.getEntity().getSurname() + ninLookupResponse.getEntity().getMiddlename();
            String requestName = userCredential.getFirstName() + userCredential.getLastName() + userCredential.getOtherName();
            char[] ninFullNameArray = ninFullName.toCharArray();
            char[] requestNameArray = requestName.toCharArray();
            Arrays.sort(ninFullNameArray);
            Arrays.sort(requestNameArray);

            if (Arrays.equals(ninFullNameArray, requestNameArray)){
                userCredential.setNinStatus("VERIFIED");
                userCredential = userCredentialRepository.save(userCredential);
                return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.BVN_VALID_CODE)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                        .responseMessage(AccountUtils.BVN_VALID_MESSAGE)
                        .build());
            }
        }
        userCredential.setNinStatus("UNVERIFIED");
        userCredential = userCredentialRepository.save(userCredential);
        return ResponseEntity.badRequest().body(CustomResponse.builder()
                .responseCode(AccountUtils.BVN_INVALID_CODE)
                .responseMessage(AccountUtils.BVN_INVALID_MESSAGE)
                .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }

    public ResponseEntity<CustomResponse> verifyEmail(EmailVerificationDto verificationDto){
        boolean existsByEmail = userCredentialRepository.existsByEmail(verificationDto.getEmail());
        UserCredential userCredential = userCredentialRepository.findByEmail(verificationDto.getEmail()).orElseThrow(RuntimeException::new);
        if (existsByEmail){
            OtpResponse response = dojahSmsService.sendOtp(OtpRequest.builder()
                            .priority(true)
                            .destination(userCredential.getPhoneNumber())
                            .length(4)
                            .expiry(4)
                            .email(verificationDto.getEmail())
                            .channel("email")
                            .senderId("ZLF")
                    .build());

            log.info("otp response\n {}", response);

            return ResponseEntity.ok(CustomResponse.builder()
                            .responseCode(AccountUtils.OTP_SENT_CODE)
                            .responseMessage(AccountUtils.OTP_SENT_MESSAGE)
                            .responseBody(response)
                    .build());
        }
        return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                        .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> validateEmail(EmailValidationDto emailValidationDto){
        UserCredential userCredential = userCredentialRepository.findByEmail(emailValidationDto.getEmail()).orElseThrow(RuntimeException::new);
        ValidateOtpResponse response = dojahSmsService.validateOtp(ValidateOtpRequest.builder()
                        .code(emailValidationDto.getCode())
                        .reference_id(emailValidationDto.getReferenceId())
                .build());

        if (response.getEntity().getValid()){
            userCredential.setEmailVerifyStatus("VERIFIED");
        } else{
            userCredential.setEmailVerifyStatus("UNVERIFIED");
        }
        UserCredential updatedUserCredential = userCredentialRepository.save(userCredential);

        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.OTP_VALIDATED_CODE)
                        .responseMessage(AccountUtils.OTP_VALIDATED_MESSAGE)
                        .responseBody(modelMapper.map(updatedUserCredential, UserCredentialResponse.class))
                .build());

    }

    public ResponseEntity<CustomResponse> loggedInUser(String email){
        boolean isEmailExists = userCredentialRepository.existsByEmail(email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();

        if (!isEmailExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.SUCCESS_CODE)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());
    }


    private CustomResponse nonExistentUserById(){
        return CustomResponse.builder()
                    .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .responseBody(null)
                    .info(null)
                    .build();

    }

    public ResponseEntity<CustomResponse> saveBiometricInfo(LoginDto loginDto){
        boolean isEmailExists = userCredentialRepository.existsByEmail(loginDto.getEmail());
        if (!isEmailExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }

        UserCredential userCredential = userCredentialRepository.findByEmail(loginDto.getEmail()).get();

        userCredential.setHashedPassword(accountUtils.encode(loginDto.getPassword(), 3));
        userCredentialRepository.save(userCredential);
        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.BIOMETRIC_INFO_SAVED_CODE)
                        .responseMessage(AccountUtils.BIOMETRIC_INFO_SAVED_MESSAGE)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());

    }

    public ResponseEntity<CustomResponse> sendOtp(OtpDto request){
        String otp = accountUtils.generateOtp();
        String referenceId = otp + UUID.randomUUID();
        LocalDateTime expiryDate = LocalDateTime.now().plus(10, ChronoUnit.MINUTES);

        boolean isEmailExist = userCredentialRepository.existsByEmail(request.getEmail());

        if (!isEmailExist){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                            .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .build());
        }


        emailService.sendEmailAlert(EmailDetails.builder()
                        .messageBody("Your pending Otp from zelia is " + otp + ". Valid for 8 minutes. PLEASE DO NOT DISCLOSE!")
                        .subject("ZELIAFINANCE")
                        .recipient(request.getEmail())
                .build());

        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.OTP_SENT_CODE)
                        .responseMessage(AccountUtils.OTP_SENT_MESSAGE)
                        .referenceId(referenceId)
                        .expiry(expiryDate)
                .build());
    }

    public ResponseEntity<CustomResponse> validateOtp(ValidateOtpDto request){
        UserCredential userCredential = userCredentialRepository.findByEmail(request.getEmail()).orElseThrow();
        if (!LocalDateTime.now().isBefore(userCredential.getOtpExpiryDate())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.OTP_EXPIRED_CODE)
                            .responseMessage(AccountUtils.OTP_EXPIRED_MESSAGE)
                            .otpStatus(false)
                    .build());
        }

        if (!request.getOtp().equals(userCredential.getOtp())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.INVALID_OTP_CODE)
                            .responseMessage(AccountUtils.INVALID_OTP_MESSAGE)
                            .otpStatus(false)
                    .build());
        }
        String token = generateToken(request.getEmail());
        userCredential.setEmailVerifyStatus("VERIFIED");
        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.OTP_VALIDATED_CODE)
                        .responseMessage(AccountUtils.OTP_VALIDATED_MESSAGE)
                        .otpStatus(true)
                        .token(token)
                        .responseBody(modelMapper.map(userCredential, UserCredentialResponse.class))
                .build());

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

}
