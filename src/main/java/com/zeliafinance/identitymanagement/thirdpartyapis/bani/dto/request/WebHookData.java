package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebHookData {
    private String pay_ref;
    private String pay_ext_ref;
    private String holder_first_name;
    private String holder_last_name;
    private CustomData custom_data;
}
