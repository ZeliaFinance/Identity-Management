package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayoutResponse {
    private String transfer_ext_ref;
    private String message;
    private String payout_ref;
    private boolean status;
    private int status_code;
}
