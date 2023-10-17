package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.loan.service.LoanFactoryService;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("smeLoanServiceFactory")
public class SmeLoanServiceFactory implements LoanFactoryService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserCredentialRepository userCredentialRepository;

    @Autowired
    public SmeLoanServiceFactory(
            LoanApplicationRepository loanApplicationRepository,
            UserCredentialRepository userCredentialRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.userCredentialRepository = userCredentialRepository;
    }

    @Override
    public LoanApplicationService createLoanService() {
        return new SmeLoanApplicationServiceImpl(loanApplicationRepository, userCredentialRepository);
    }
}
