package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CACAdvanceRequest {

    private String  rc;
    private String type;

}
