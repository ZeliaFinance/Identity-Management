package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.loan.service.LoanFactoryService;
import org.springframework.stereotype.Component;

@Component("personalLoanServiceFactory")
public class PersonalLoanServiceFactory implements LoanFactoryService {


    @Override
    public LoanApplicationService createLoanService() {
        return new PersonalLoanApplicationService();
    }
}
