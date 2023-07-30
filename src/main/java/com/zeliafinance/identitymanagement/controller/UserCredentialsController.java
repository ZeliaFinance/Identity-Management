package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@Slf4j
public class UserCredentialsController {

    private final AuthService service;


    public UserCredentialsController(AuthService service){
        this.service = service;
    }

    @PostMapping("/registerUser")
    public ResponseEntity<CustomResponse> registerUser(@RequestBody SignUpRequest request){
        return service.signUp(request);
    }

    @PutMapping("/updateUser/{userId}")
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

}
