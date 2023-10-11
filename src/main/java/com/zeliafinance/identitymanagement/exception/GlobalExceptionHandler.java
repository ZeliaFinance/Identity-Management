package com.zeliafinance.identitymanagement.exception;

import com.zeliafinance.identitymanagement.dto.ErrorDetails;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(AuthenticationException exception, WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now().toString(), exception.getMessage(), webRequest.getDescription(false));
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception
                                                                          exception, WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now().toString(), exception.getMessage(), webRequest.getDescription(false));
        return ResponseEntity.internalServerError().body(errorDetails);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDetails> handleExpiredJwtException(ExpiredJwtException exception, WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now().toString(), exception.getMessage(), webRequest.getDescription(true));
        return ResponseEntity.internalServerError().body(errorDetails);
    }



}
