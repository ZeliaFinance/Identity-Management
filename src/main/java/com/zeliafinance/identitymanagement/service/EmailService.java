package com.zeliafinance.identitymanagement.service;

import com.zeliafinance.identitymanagement.dto.EmailDetails;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
}
