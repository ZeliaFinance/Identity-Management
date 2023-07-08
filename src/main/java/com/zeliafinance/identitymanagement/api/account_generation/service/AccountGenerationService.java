package com.zeliafinance.identitymanagement.api.account_generation.service;

import com.zeliafinance.identitymanagement.api.account_generation.dto.request.AccountGenerationRequest;
import com.zeliafinance.identitymanagement.api.account_generation.dto.response.AccountGenerationResponse;

public interface AccountGenerationService {
    public Object createAccount(AccountGenerationRequest accountGenerationRequest);
}
