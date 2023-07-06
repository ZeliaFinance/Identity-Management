package com.zeliafinance.identitymanagement.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class AccountUtils {
    public static final String BVN_EXISTS_CODE = "001";
    public static final String BVN_EXISTS_MESSAGE = "User with this bvn already exists";
    public static final String EMAIL_EXISTS_CODE = "002";
    public static final String EMAIL_EXISTS_MESSAGE = "User witht this email address exists";
    public static final String IDENTITY_EXISTS_CODE = "003";
    public static final String IDENTITY_EXISTS_MESSAGE = "User with this identity already exists";
    public static final int LENGTH_OF_ACCOUNT_NUMBER = 10;
    public static final int LENGTH_OF_PASSWORD = 8;
    public static final String ACCOUNT_CREATION_SUCCESS_CODE = "004";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "User Account has been successfully created!";
    public static final String ACCOUNT_CREATION_ALERT_SUBJECT = "ACCOUNT CREATION ALERT!!!";
    public static final String USER_NOT_EXIST_CODE = "005";
    public static final String USER_NOT_EXIST_MESSAGE = "Sign up first. Then proceed to update your profile";
    public static final String USER_ROLE_SET_CODE = "006";
    public static final String USER_ROLE_SET_MESSAGE = "User role has been successfully updated";
    public static final String LOGIN_SUCCESS_CODE = "007";
    public static final String LOGIN_SUCCESS_MESSAGE = "Successfully Logged in.";
    public static final String ROLE_UPDATE_SUCCESS_CODE = "008";
    public static final String ROLE_UPDATE_SUCCESS_MESSAGE = "User role updated!";

    @Bean
    public String generateAccountNumber(){
        StringBuilder accountNumber = new StringBuilder();
        Random random = new Random();
        int count = 0;
        while(count < LENGTH_OF_ACCOUNT_NUMBER){
            accountNumber.append(random.nextInt(10));
            ++count;
        }
        return accountNumber.toString();
    }


    public String generatePassword(){
        StringBuilder password = new StringBuilder();
        int count = 0;
        Random randomPassword = new Random();
        while (count < LENGTH_OF_PASSWORD){
            password.append((char) randomPassword.nextInt(33, 127));
            count++;
        }
        return password.toString();
    }

}
