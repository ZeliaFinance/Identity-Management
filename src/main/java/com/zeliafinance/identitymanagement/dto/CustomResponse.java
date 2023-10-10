package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomResponse{
    private int statusCode;
    private String responseMessage;
    private Object responseBody;
    private String token;
    private String hashedPassword;
    private String hashedPin;
    private Info info;
    private Boolean otpStatus;
    private String referenceId;
    private LocalDateTime expiry;

}
