package com.zeliafinance.identitymanagement.loan.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanProductRequest;
import com.zeliafinance.identitymanagement.loan.service.LoanProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/loanProduct")
@AllArgsConstructor
public class LoanProductController {

    LoanProductService service;

    @PostMapping
    public ResponseEntity<CustomResponse> addLoanProduct(@RequestBody LoanProductRequest request){
        return service.saveLoanProduct(request);
    }

    @GetMapping
    public ResponseEntity<CustomResponse> fetchAllProducts(){
        return service.fetchAllLoanProducts();
    }

    @GetMapping("product")
    public ResponseEntity<CustomResponse> fetchProductByProductName(@RequestParam String loanProductName) throws Exception {
        return service.fetchLoanProductByProductName(loanProductName);
    }

    @GetMapping("{productId}")
    public ResponseEntity<CustomResponse> fetchProductById(@PathVariable Long productId) throws Exception {
        return service.fetchLoanProductById(productId);
    }

    @PutMapping("{productId}")
    public ResponseEntity<CustomResponse> updateProduct(@PathVariable Long productId, @RequestBody LoanProductRequest request) throws Exception {
        return service.updateLoanProduct(request, productId);
    }

    @DeleteMapping("{productId}")
    public ResponseEntity<CustomResponse> deleteProduct(@PathVariable Long productId) throws Exception {
        return service.deleteLoanProduct(productId);
    }

    @GetMapping("loanProducts")
    public ResponseEntity<CustomResponse> fetchDistinctLoanProducts(){
        return service.fetchDistinctLoanProductNames();
    }

    @GetMapping("inactiveLoanProducts")
    public ResponseEntity<CustomResponse> fetchInactiveLoanProducts(){
        return service.fetchInactiveLoans();
    }
}
