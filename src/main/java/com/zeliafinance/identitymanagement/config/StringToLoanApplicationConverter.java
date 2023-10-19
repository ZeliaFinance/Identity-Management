package com.zeliafinance.identitymanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLoanApplicationConverter implements Converter<String, LoanApplication> {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    @SneakyThrows
    public LoanApplication convert(String source) {
        return objectMapper.readValue(source, LoanApplication.class);
    }
}
