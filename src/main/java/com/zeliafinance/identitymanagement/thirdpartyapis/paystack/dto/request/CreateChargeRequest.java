package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateChargeRequest {
    private String email;
    private String amount;
    private MetaData metaData;
    private Card card;
}
