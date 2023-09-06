package com.zeliafinance.identitymanagement.thirdpartyapis.termii.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsResponse {
    private String message_id;
    private String message;
    private int balance;
    private String user;
}
