package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidateOtpRequest {
    private String code;
    private String reference_id;
}
