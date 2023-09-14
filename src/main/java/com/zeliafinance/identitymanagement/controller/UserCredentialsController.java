package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@Slf4j
@CrossOrigin(origins = {"https://www.sandbox.zeliafinance.com", "http://www.sandbox.zeliafinance.com"}, maxAge = 3600)
public class UserCredentialsController {

    private final AuthService service;


    public UserCredentialsController(AuthService service){
        this.service = service;
    }

    @PostMapping("/registerUser")
    public ResponseEntity<CustomResponse> registerUser(@RequestBody SignUpRequest request){
        return service.signUp(request);
    }

    @PatchMapping("/updateUser/{userId}")
    public ResponseEntity<CustomResponse> updateProfile(@RequestBody UserProfileRequest request, @PathVariable(name = "userId") Long userId){
        return service.updateUserProfile(userId, request);
    }

    @PostMapping("/login")
    public ResponseEntity<CustomResponse> login(@RequestBody LoginDto loginDto){
        return service.login(loginDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CustomResponse> fetchAllUsers(
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "50") int pageSize

    ){
        return service.fetchAllUsers(pageNo, pageSize);
    }

    @GetMapping("{userId}")
    public ResponseEntity<CustomResponse> fetchUser(@PathVariable (value = "userId") Long userId){
        return service.fetchUser(userId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/updateRole/{userId}")
    public ResponseEntity<CustomResponse> updateUserRole(@PathVariable (value = "userId") Long userId){
        return service.updateUserRole(userId);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<CustomResponse> sendResetPasswordLink(@RequestParam String email){
        return service.resetPassword(email);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<CustomResponse> changePassword(@RequestParam String email, @RequestBody PasswordResetDto passwordResetDto){
        return service.changePassword(email, passwordResetDto);
    }

    @GetMapping("/generateReferralLink")
    public ResponseEntity<CustomResponse> generateReferralLink(@RequestParam String email){
        return service.generateReferralLink(email);
    }

    @PostMapping("/verifyId")
    public ResponseEntity<CustomResponse> verifyIdentity(@RequestBody NinVerificationDto identityVerificationDto){
        return service.verifyCustomerIdentity(identityVerificationDto);
    }

    @PostMapping("/verifyBvn")
    public ResponseEntity<CustomResponse> verifyBvn(@RequestBody BvnVerificationDto bvnVerificationDto){
        return service.verifyBvn(bvnVerificationDto);
    }

    @PostMapping("/verifyNin")
    public ResponseEntity<CustomResponse> verifyNin(@RequestBody NinVerificationDto ninVerificationDto){
        return service.verifyNin(ninVerificationDto);
    }

    @PostMapping("/saveBiometricInfo")
    public ResponseEntity<CustomResponse> biometricInfo(@RequestBody LoginDto loginDto){
        return service.saveBiometricInfo(loginDto);
    }

    @PostMapping("/sendOtp")
    public ResponseEntity<CustomResponse> sendOtp(@RequestBody OtpDto otpDto){
        return service.sendOtp(otpDto);
    }

    @PostMapping("/validateOtp")
    public ResponseEntity<CustomResponse> validateOtp(@RequestBody ValidateOtpDto otpDto){
        return service.validateOtp(otpDto);
    }

    @GetMapping("/loggedInUser")
    public ResponseEntity<CustomResponse> getLoggedInUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.loggedInUser(email);
    }

    @PostMapping("/createPin")
    public ResponseEntity<CustomResponse> createPin(@RequestBody PinSetupDto pinSetupDto){
        return service.pinSetup(pinSetupDto);
    }

    @PostMapping("/verifyPin")
    public ResponseEntity<CustomResponse> verifyPin(@RequestBody PinSetupDto pinSetupDto){
        return service.verifyPin(pinSetupDto);
    }

}
