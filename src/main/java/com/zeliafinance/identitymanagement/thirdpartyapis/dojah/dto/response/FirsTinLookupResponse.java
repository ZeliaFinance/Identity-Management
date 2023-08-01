package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.response;

import com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.FirsTinData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirsTinLookupResponse {
    private FirsTinData entity;
}
