package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.config.JwtTokenProvider;
import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.IdentityType;
import com.zeliafinance.identitymanagement.entity.Role;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.service.DojahSmsService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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

        UserCredential userCredential = UserCredential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .walletId(accountUtils.generateAccountNumber())
                .emailVerifyStatus("UNVERIFIED")
                .referralCode(accountUtils.generateReferralCode())
                .referredBy(request.getReferredBy())
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



    public ResponseEntity<CustomResponse> updateUserProfile(Long userId, UserProfileRequest request){
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


            DojahBvnResponse bvnResponse = dojahSmsService.basicBvnLookUp(BvnRequest.builder()
                            .bvn(request.getBvn())
                    .build());
            String bvnFullName = bvnResponse.getEntity().getFirstName() + bvnResponse.getEntity().getLastName() + bvnResponse.getEntity().getMiddleName();
            String requestName = request.getFirstName() + request.getLastName() + request.getOtherName();
            char[] bvnFullNameArray = bvnFullName.toCharArray();
            char[] requestNameArray = requestName.toCharArray();
            int sumBvnChars = 0;
            int sumRequestChars = 0;
            for(char c : bvnFullNameArray){
                sumBvnChars += c;
            }
            for (char c : requestNameArray){
                sumRequestChars += c;
            }
            if (sumBvnChars == sumRequestChars){
                userCredential.setBvn("VERIFIED");
            } else {
                userCredential.setBvn("UNVERIFIED");
            }

            userCredential.setIdentityType(IdentityType.valueOf(request.getIdentityType()));
            userCredential.setIdentityNumber(request.getIdentityNumber());
            userCredential.setPin(request.getPin());
            userCredential.setAccountStatus("PENDING");
            OtpResponse otpResponse = dojahSmsService.sendOtp(OtpRequest.builder()
                            .senderId("ZELIA FINANCE")
                            .channel("email")
                            .email(userCredential.getEmail())
                            .expiry(4)
                            .length(4)
                            .priority(true)
                    .build());
            ValidateOtpResponse validateOtpResponse = dojahSmsService.validateOtp(ValidateOtpRequest.builder()
                            .reference_id(otpResponse.getEntity().getReferenceId())
                            .code(request.getCode())
                    .build());
            if (!validateOtpResponse.getEntity().getValid()){
                userCredential.setEmailVerifyStatus("UNVERIFIED");
            } else {
                userCredential.setEmailVerifyStatus("VERIFIED");
            }

            userCredential.setDeviceIp(request.getDeviceIp());
            userCredential.setLiveLocation(request.getLiveLocation());
            userCredential.setModifiedby(SecurityContextHolder.getContext().getAuthentication().getName());
            userCredential.setReferredBy(request.getReferredBy());

            //Verifying via NIN
            if (request.getIdentityType().equalsIgnoreCase(String.valueOf(IdentityType.NIN))){
                NinLookupResponse ninLookupResponse = dojahSmsService.ninLookup(NinRequest.builder()
                        .nin(request.getIdentityNumber())
                        .build());
                String ninFullName = ninLookupResponse.getEntity().getFirstname() + ninLookupResponse.getEntity().getMiddlename() + ninLookupResponse.getEntity().getSurname();
                char[] ninFullNameArray = ninFullName.toCharArray();
                int sumNinChars = 0;
                for (char c: ninFullNameArray){
                    sumNinChars += c;
                }
                boolean b = (sumNinChars == sumRequestChars) && (request.getDateOfBirth().equals(LocalDate.parse(ninLookupResponse.getEntity().getBirthdate(), DateTimeFormatter.ISO_DATE))) && request.getGender().equalsIgnoreCase(ninLookupResponse.getEntity().getGender());
                if (b){
                    userCredential.setIdentityStatus("VERIFIED");
                } else {
                    userCredential.setIdentityStatus("UNVERIFIED");
                }
            }
            //verifying via driver license
            if (request.getIdentityType().equalsIgnoreCase(String.valueOf(IdentityType.DRIVER_LICENSE))){
                DriverLicenseResponse dlResponse = dojahSmsService.dlLookup(DriverLicenseRequest.builder()
                                .licenseNumber(request.getIdentityNumber())
                        .build());
                String dlName = dlResponse.getEntity().getFirstName() + dlResponse.getEntity().getLastName() + dlResponse.getEntity().getMiddleName();
                char[] dlNameArray = dlName.toCharArray();
                int sumdlNameChars = 0;
                for (char c : dlNameArray){
                    sumdlNameChars += c;
                }
                boolean b = (sumdlNameChars == sumRequestChars) && (request.getDateOfBirth().equals(LocalDate.parse(dlResponse.getEntity().getBirthDate(), DateTimeFormatter.ISO_DATE))) && request.getGender().equalsIgnoreCase(dlResponse.getEntity().getGender());
                if (b){
                    userCredential.setIdentityStatus("VERIFIED");
                } else {
                    userCredential.setIdentityStatus("UNVERIFIED");
                }
            }

            //verifying via pvc
            if (request.getIdentityType().equalsIgnoreCase(String.valueOf(IdentityType.PVC))){
                PvcResponse pvcResponse = dojahSmsService.pvcLookup(PvcRequest.builder()
                        .vin(request.getIdentityNumber())
                        .build());
                String pvcName = pvcResponse.getEntity().getFullName();
                char[] pvcNameArray = pvcName.toCharArray();
                int sumPvcNameChars = 0;
                for (char c : pvcNameArray){
                    sumPvcNameChars += c;
                }
                boolean b = (sumPvcNameChars == sumRequestChars) && (request.getDateOfBirth().equals(LocalDate.parse(pvcResponse.getEntity().getDateOfBirth(), DateTimeFormatter.ISO_DATE))) && request.getGender().equalsIgnoreCase(pvcResponse.getEntity().getGender());
                if (b){
                    userCredential.setIdentityStatus("VERIFIED");
                } else {
                    userCredential.setIdentityStatus("UNVERIFIED");
                }
            }

            //verifying international passport

            if (request.getIdentityType().equalsIgnoreCase(String.valueOf(IdentityType.INTERNATIONAL_PASSPORT))){
                IntPassportResponse intPassportResponse = dojahSmsService.intPassportLookup(IntPassportRequest.builder()
                        .passportNumber(request.getIdentityNumber())
                                .surname(request.getLastName())
                        .build());
                String intPassportName = intPassportResponse.getEntity().getSurname() + intPassportResponse.getEntity().getFirstName() + intPassportResponse.getEntity().getOtherNames();
                char[] intPassportNameArray = intPassportName.toCharArray();
                int sumIntPassportNameChars = 0;
                for (char c : intPassportNameArray){
                    sumIntPassportNameChars += c;
                }
                boolean b = (sumIntPassportNameChars == sumRequestChars) && (request.getDateOfBirth().equals(LocalDate.parse(intPassportResponse.getEntity().getDateOfBirth(), DateTimeFormatter.ISO_DATE))) && request.getGender().equalsIgnoreCase(intPassportResponse.getEntity().getGender());
                if (b){
                    userCredential.setIdentityStatus("VERIFIED");
                } else {
                    userCredential.setIdentityStatus("UNVERIFIED");
                }
            }



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

        return ResponseEntity.ok(CustomResponse.builder()
                .responseCode(AccountUtils.LOGIN_SUCCESS_CODE)
                .responseMessage(AccountUtils.LOGIN_SUCCESS_MESSAGE)
                .responseBody(jwtTokenProvider.generateToken(authentication))
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
        String token = UUID.randomUUID().toString();
        userCredential.setPasswordResetToken(token);
        userCredential.setTokenExpiryDate(LocalDate.now().plusDays(1));
        String url = "/users/changePassword?token="+token;
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(email)
                .subject(AccountUtils.PASSWORD_RESET_SUBJECT)
                .messageBody("follow this link to change your password " + url)
                .build();
        emailService.sendEmailAlert(emailDetails);

        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.PASSWORD_RESET_CODE)
                        .responseMessage(AccountUtils.PASSWORD_RESET_MESSAGE)
                .build());
    }

    public ResponseEntity<CustomResponse> changePassword(String email, PasswordResetDto passwordResetDto){
        boolean isUserExists = userCredentialRepository.existsByEmail(email);
        if (!isUserExists){
            nonExistentUserById();
        }
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        if (userCredential.getTokenExpiryDate().isBefore(LocalDate.now())){
            return ResponseEntity.internalServerError().body(CustomResponse.builder()
                            .responseCode(AccountUtils.PASSWORD_TOKEN_EXPIRED_CODE)
                            .responseMessage(AccountUtils.PASSWORD_TOKEN_EXPIRED_MESSAGE)
                    .build());
        }
        if (!passwordResetDto.getNewPassword().equals(passwordResetDto.getConfirmNewPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.PASSWORD_INCORRECT_CODE)
                            .responseMessage(AccountUtils.PASSWORD_INCORRECT_MESSAGE)
                    .build());
        }
        userCredential.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
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


    private CustomResponse nonExistentUserById(){
        return CustomResponse.builder()
                    .responseCode(AccountUtils.USER_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.USER_NOT_EXIST_MESSAGE)
                    .responseBody(null)
                    .info(null)
                    .build();

    }
}
