package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.ChangePinRequest;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.PasswordChangeRequest;
import com.zeliafinance.identitymanagement.service.impl.ChangePinPassword;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/securitySetUp")
@AllArgsConstructor
public class ChangePinPasswordController {

    private ChangePinPassword changePinPassword;

    @PostMapping("/verifySecurityQuestion")
    public ResponseEntity<CustomResponse> securityQuestion(@RequestBody ChangePinRequest changePinRequest) throws MessagingException, TemplateException, IOException {
        return changePinPassword.checkSecurityQuestionResponse(changePinRequest);
    }

    @PostMapping("/changePin")
    public ResponseEntity<CustomResponse> changePin(@RequestBody ChangePinRequest changePinRequest){
        return changePinPassword.changePin(changePinRequest);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<CustomResponse> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest){
        return changePinPassword.changePassword(passwordChangeRequest);
    }

}
