package com.zeliafinance.identitymanagement.loan.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.service.impl.PersonalLoanServiceFactory;
import com.zeliafinance.identitymanagement.loan.service.impl.SmeLoanServiceFactory;
import com.zeliafinance.identitymanagement.loan.service.impl.TuitionLoanServiceFactory;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Getter
@Service
public class LoanRoutingService {
    @Autowired
    @Qualifier("smeLoanServiceFactory")
    private LoanFactoryService loanFactoryService;

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserCredentialRepository userCredentialRepository;

    public LoanRoutingService (LoanApplicationRepository loanApplicationRepository,
                               UserCredentialRepository userCredentialRepository){
        this.loanApplicationRepository = loanApplicationRepository;
        this.userCredentialRepository = userCredentialRepository;
    }


    public ResponseEntity<CustomResponse> processLoanApplication(String loanType, LoanApplicationRequest request) throws Exception {
        LoanFactoryService factoryService = getFactoryForLoanType(loanType);
        LoanApplicationService service = factoryService.createLoanService();
        return service.applyForLoan(request);
    }

    private LoanFactoryService getFactoryForLoanType(String loanType){
        if (loanType.equalsIgnoreCase("SME Loan")){
            return new SmeLoanServiceFactory(loanApplicationRepository, userCredentialRepository);
        } else if (loanType.equalsIgnoreCase("Tuition Loan")){
            return new TuitionLoanServiceFactory();
        } else {
            return new PersonalLoanServiceFactory();
        }
    }

    public void setLoanFactoryService(LoanFactoryService loanFactoryService) {
        this.loanFactoryService = loanFactoryService;
    }
}
