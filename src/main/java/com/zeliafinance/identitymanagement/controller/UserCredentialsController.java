package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
public class UserCredentialsController {

    private AuthService service;

    @Autowired
    EmailService emailService;

    public UserCredentialsController(AuthService service){
        this.service = service;
    }

    @PostMapping("/registerUser")
    public CustomResponse registerUser(@RequestBody SignUpRequest request){
        return service.signUp(request);
    }

    @PutMapping("/updateUser/{userId}")
    public CustomResponse updateProfile(@RequestBody UserProfileRequest request, @PathVariable(name = "userId") Long userId){
        return service.updateUserProfile(userId, request);
    }

    @PostMapping("/login")
    public CustomResponse login(@RequestBody LoginDto loginDto){
        return service.login(loginDto);
    }

//    @PutMapping("/updateUserRole/{userId}")
//    public CustomResponse updateUserRole(@RequestBody UpdateRoleRequest request, @PathVariable(name = "userId") Long userId){
//        return service.updateUserRole(request, userId);
//    }


//    @PostMapping("/sendMail")
//    public void sendMail(@RequestBody EmailDetails emailDetails){
//        emailService.sendEmailAlert(emailDetails);
//    }
}
