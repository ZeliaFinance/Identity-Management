package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.SampleJsonObject;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class HomeController {

    AuthService authService;

//    @GetMapping
//    public SampleJsonObject serveHomePage(){
//        SampleJsonObject jsonObject = new SampleJsonObject();
//        jsonObject.setServerStatus("Running");
//        jsonObject.setSampleProperty("Some Value");
//        return jsonObject;
//    }
}