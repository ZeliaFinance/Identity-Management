package com.zeliafinance.identitymanagement.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
    private long numberOfUsers;
    private long verifiedUsers;
    private long nonVerifiedUsers;
}
