package com.zeliafinance.identitymanagement.thirdpartyapis.dojah.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NinRequest {
    private String nin;
}
