package com.zeliafinance.identitymanagement.banks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NameEnquiryRequest {
    private String bankName;
    private String accountNumber;
}
