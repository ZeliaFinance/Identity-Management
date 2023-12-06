package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingResponse;
import com.zeliafinance.identitymanagement.loan.dto.OfferingByLoanProductResponse;
import com.zeliafinance.identitymanagement.loan.entity.LoanOffering;
import com.zeliafinance.identitymanagement.loan.repository.LoanOfferingRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanOfferingService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LoanOfferingServiceImpl implements LoanOfferingService {

    private LoanOfferingRepository loanOfferingRepository;
    private ModelMapper modelMapper;
    @Override
    public ResponseEntity<CustomResponse> saveLoanOffering(LoanOfferingRequest loanOfferingRequest) {
        LoanOffering loanOffering = LoanOffering.builder()
                .loanProduct(loanOfferingRequest.getLoanProduct())
                .minAmount(loanOfferingRequest.getMinAmount())
                .maxAmount(loanOfferingRequest.getMaxAmount())
                .interestRate(loanOfferingRequest.getInterestRate())
                .daysAvailable(loanOfferingRequest.getDaysAvailable())
                .build();
        LoanOffering savedLoanOffering = loanOfferingRepository.save(loanOffering);
        LoanOfferingResponse response = modelMapper.map(savedLoanOffering, LoanOfferingResponse.class);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(response)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchAllLoanOfferings() {
        Map<String, List<LoanOfferingResponse>> loanOfferings = loanOfferingRepository.findAll().stream()
                .map(loanOffering -> modelMapper.map(loanOffering, LoanOfferingResponse.class))
                .collect(Collectors.groupingBy(LoanOfferingResponse::getLoanProduct));
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanOfferings)
                .build());

    }

    @Override
    public List<LoanOfferingResponse> fetchLoanOfferingByProductName(String loanProduct) {
        List<LoanOfferingResponse> loans = loanOfferingRepository.findByLoanProduct(loanProduct).stream().map(loanOffering -> modelMapper.map(loanOffering, LoanOfferingResponse.class)).toList();

        // Group by loanProduct, interestRate, minAmount, and maxAmount
        Map<List<Object>, List<LoanOfferingResponse>> grouped = loans.stream()
                .collect(Collectors.groupingBy(loan -> Arrays.asList(loan.getLoanProduct(), loan.getInterestRate(), loan.getMinAmount(), loan.getMaxAmount())));

        // Create a new list of aggregated loans
        List<LoanOfferingResponse> aggregatedLoans = new ArrayList<>();
        for (Map.Entry<List<Object>, List<LoanOfferingResponse>> entry : grouped.entrySet()) {
            List<Object> key = entry.getKey();
            List<LoanOfferingResponse> groupedLoans = entry.getValue();

            // Aggregate daysAvailable into a list
            List<Integer> daysAvailable = groupedLoans.stream()
                    .map(LoanOfferingResponse::getDaysAvailable)
                    .collect(Collectors.toList());

            // Create a new loan with the aggregated data
            LoanOfferingResponse aggregatedLoan = new LoanOfferingResponse((String) key.get(0), (Double) key.get(1), daysAvailable, (Double) key.get(2), (Double) key.get(3));
            aggregatedLoans.add(aggregatedLoan);
        }

        return aggregatedLoans;


    }

    private OfferingByLoanProductResponse createLoanProductDetails(List<LoanOffering> loanOfferings) {
        LoanOffering firstOffering = loanOfferings.get(0);
        return OfferingByLoanProductResponse.builder()
                .loanProduct(firstOffering.getLoanProduct())
                .loanBrackets(loanOfferings.stream().map(LoanOffering::getDaysAvailable).collect(Collectors.toList()))
                .interestRate(firstOffering.getInterestRate())
                .minAmount(firstOffering.getMinAmount())
                .maxAmount(firstOffering.getMaxAmount())
                .build();
    }
}
