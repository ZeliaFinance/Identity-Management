package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.dto;

import com.amazonaws.services.ecr.model.AuthorizationData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Meta {
    private AuthorisationData authorization;

}
