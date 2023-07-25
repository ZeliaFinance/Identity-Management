package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DojahSmsRequest {
    private String destination;
    private String message;
    private String channel;
    private Boolean priority;
    private String sender_id;
}
