package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")

public class UserCredentialsController {

    private final AuthService service;


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

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CustomResponse fetchAllUsers(
            @RequestParam(value = "pageNo") int pageNo,
            @RequestParam(value = "pageSize") int pageSize

    ){
        return service.fetchAllUsers(pageNo, pageSize);
    }

    @GetMapping("{userId}")
    public CustomResponse fetchUser(@PathVariable (value = "userId") Long userId){
        return service.fetchUser(userId);
    }

    @PutMapping("/updateRole/{userId}")
    public CustomResponse updateUserRole(@PathVariable (value = "userId") Long userId){
        return service.updateUserRole(userId);
    }

}
