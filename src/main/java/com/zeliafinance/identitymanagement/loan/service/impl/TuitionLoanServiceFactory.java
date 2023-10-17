package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.loan.service.LoanFactoryService;
import org.springframework.stereotype.Component;

@Component("tuitionLoanServiceFactory")
public class TuitionLoanServiceFactory implements LoanFactoryService {


    @Override
    public LoanApplicationService createLoanService() {
        return null;
    }
}
