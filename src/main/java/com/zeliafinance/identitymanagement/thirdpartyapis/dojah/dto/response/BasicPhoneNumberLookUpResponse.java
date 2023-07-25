package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.PhoneNumberData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicPhoneNumberLookUpResponse {
    private PhoneNumberData entity;
}
