package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.entity.LoanProduct;
import com.zeliafinance.identitymanagement.loan.repository.LoanProductRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.zeliafinance.identitymanagement.utils.AccountUtils.*;
@Service
@AllArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private LoanProductRepository loanProductRepository;
    @Override
    public ResponseEntity<CustomResponse> applyForLoan(LoanApplicationRequest request) {

        return null;
    }

    @Override
    public ResponseEntity<CustomResponse> calculateLoan(LoanApplicationRequest request) {

        BigDecimal amountToPay = BigDecimal.ZERO;
        List<LoanProduct> loanProducts = loanProductRepository.findAll().stream().filter(loanProduct -> loanProduct.getLoanProductName().equalsIgnoreCase(request.getLoanType())).toList();
        for (LoanProduct loanProduct : loanProducts){
            if (request.getLoanAmount().doubleValue() >= loanProduct.getMinAmount().doubleValue() && request.getLoanAmount().doubleValue() <= loanProduct.getMaxAmount().doubleValue()){
                amountToPay = request.getLoanAmount().add(BigDecimal.valueOf((request.getLoanAmount().doubleValue() * ((double)loanProduct.getInterestRate() / 100))/100));
            }
        }

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(amountToPay)
                .build());

    }
}
