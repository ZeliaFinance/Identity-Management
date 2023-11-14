package com.zeliafinance.identitymanagement.admin.service;

import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminService {
    private UserCredentialRepository userCredentialRepository;

}
