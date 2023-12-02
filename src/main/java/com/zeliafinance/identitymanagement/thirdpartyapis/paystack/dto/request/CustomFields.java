package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomFields {
    private String value;
    private String display_name;
    private String variable_name;
}
