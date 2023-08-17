package com.zeliafinance.identitymanagement.dto;

import lombok.Data;

@Data
public class SampleJsonObject {
    //POJO - Plain Old Java Objects
    private boolean error;
    private ErrorData[] errors;
    private String message;
    private ErrorData data;
    private int statusCode;

}
