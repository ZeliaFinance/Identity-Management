package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFundResponse {
    private boolean status;
    private String message;
    private FundData fundData;

}
