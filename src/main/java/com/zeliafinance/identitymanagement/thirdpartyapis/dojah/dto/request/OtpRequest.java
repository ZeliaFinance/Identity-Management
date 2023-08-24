package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpRequest {
    @JsonProperty("sender_id")
    private String senderId;
    @Nullable
    private String destination;
    private String channel;
    @Nullable
    private String email;
    private Integer expiry;
    private Integer length;
    private Boolean priority;
}
