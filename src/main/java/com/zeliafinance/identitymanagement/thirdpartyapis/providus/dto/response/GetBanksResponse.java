package com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.response;

import com.zeliafinance.identitymanagement.thirdpartyapis.providus.dto.Banks;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetBanksResponse {
    private List<Banks> banks;
}
