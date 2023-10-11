package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.Info;
import com.zeliafinance.identitymanagement.loan.dto.LoanProductRequest;
import com.zeliafinance.identitymanagement.loan.entity.LoanProduct;
import com.zeliafinance.identitymanagement.loan.repository.LoanProductRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static com.zeliafinance.identitymanagement.utils.AccountUtils.SUCCESS_MESSAGE;

@Service
@Slf4j
@AllArgsConstructor
public class LoanProductServiceImpl implements LoanProductService {

    private LoanProductRepository loanProductRepository;

    @Override
    public ResponseEntity<CustomResponse> saveLoanProduct(LoanProductRequest request) {
        LoanProduct loanProduct = LoanProduct.builder()
                .loanProductName(request.getLoanProductName())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .minDuration(request.getMinDuration())
                .maxDuration(request.getMaxDuration())
                .interestRate(request.getInterestRate())
                .createdBy(SecurityContextHolder.getContext().getAuthentication().getName())
                .modifiedBy(SecurityContextHolder.getContext().getAuthentication().getName())
                .status("ACTIVE")
                .build();

        LoanProduct savedProduct = loanProductRepository.save(loanProduct);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(savedProduct)
                .build());
    }

    public ResponseEntity<CustomResponse> fetchAllLoanProducts(){
        List<LoanProduct> productList = loanProductRepository.findAll().stream().filter(loanProduct -> loanProduct.getStatus().equalsIgnoreCase("ACTIVE")).toList();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(productList)
                        .info(Info.builder()
                                .totalElements((long) productList.size())
                                .build())
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchLoanProductByProductName(String loanProductName) throws Exception {
        List<LoanProduct> loanProducts = loanProductRepository.findAll()
                .stream()
                .filter(product -> product.getLoanProductName().equalsIgnoreCase(loanProductName)
                        && product.getStatus().equalsIgnoreCase("ACTIVE"))
                .sorted(Comparator.comparing(LoanProduct::getMinAmount))
                .toList();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(loanProducts)
                        .info(Info.builder()
                                .totalElements((long)loanProducts.size())
                                .build())
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchLoanProductById(Long productId) throws Exception {
        LoanProduct loanProduct = loanProductRepository.findById(productId).get();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(loanProduct)
                .build());
    }


    @Override
    public ResponseEntity<CustomResponse> updateLoanProduct(LoanProductRequest request, Long productId) throws Exception {
        LoanProduct loanProduct = loanProductRepository.findById(productId).orElseThrow(Exception::new);
        if (request.getLoanProductName() != null){
            loanProduct.setLoanProductName(request.getLoanProductName());
        }
        if (request.getMinAmount() != 0){
            loanProduct.setMinAmount(request.getMinAmount());
        }
        if (request.getMaxAmount() != 0){
            loanProduct.setMaxAmount(request.getMaxAmount());
        }
        if (request.getMinDuration() != 0){
            loanProduct.setMinDuration(request.getMinDuration());
        }
        if (request.getMaxDuration() != 0){
            loanProduct.setMaxDuration(request.getMaxDuration());
        }
        if (request.getInterestRate() != 0.0){
            loanProduct.setInterestRate(request.getInterestRate());
        }
        loanProduct.setModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        LoanProduct updatedProduct = loanProductRepository.save(loanProduct);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(updatedProduct)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> deleteLoanProduct(Long productId) throws Exception {
        LoanProduct loanProduct = loanProductRepository.findById(productId).orElseThrow(Exception::new);

        loanProduct.setStatus("INACTIVE");
        loanProduct = loanProductRepository.save(loanProduct);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(loanProduct)
                .build());

    }

    @Override
    public ResponseEntity<CustomResponse> fetchDistinctLoanProductNames() {
        List<String> loanProducts = loanProductRepository.findAll().stream().map(LoanProduct::getLoanProductName).distinct().toList();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(loanProducts)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchInactiveLoans() {
        List<LoanProduct> loanProducts = loanProductRepository.findAll().stream()
                .filter(loanProduct -> loanProduct.getStatus().equalsIgnoreCase("INACTIVE"))
                .toList();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(loanProducts)
                .build());
    }


}
