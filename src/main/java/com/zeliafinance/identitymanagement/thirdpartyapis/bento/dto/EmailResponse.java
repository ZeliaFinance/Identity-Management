package com.zeliafinance.identitymanagement.thirdpartyapis.bento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailResponse {
    private int results;
    private int failed;
}
