package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.config.JwtTokenProvider;
import com.zeliafinance.identitymanagement.dto.SampleJsonObject;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class HomeController {

    AuthService authService;
    JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public SampleJsonObject serveHomePage(){
        SampleJsonObject jsonObject = new SampleJsonObject();
        jsonObject.setMessage("Server is healthy");
        jsonObject.setData(null);
        jsonObject.setStatusCode(200);
        return jsonObject;
    }

    @GetMapping("login")
    public String logout(Authentication authentication){
        return "you're logged out";
    }
}
