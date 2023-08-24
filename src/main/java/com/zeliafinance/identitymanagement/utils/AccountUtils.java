package com.zeliafinance.identitymanagement.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AccountUtils {
    public static final String BVN_EXISTS_CODE = "001";
    public static final String BVN_EXISTS_MESSAGE = "User with this bvn already exists";
    public static final String EMAIL_EXISTS_CODE = "002";
    public static final String EMAIL_EXISTS_MESSAGE = "User with this email address exists";
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
    public static final String PASSWORD_INCORRECT_CODE  = "009";
    public static final String PASSWORD_INCORRECT_MESSAGE= "Disparity in Password and Confirm Password fields";
    public static final String ACCOUNT_CONFIRMATION_MESSAGE = "Congrats! Your account has been successfully created. Now click on this link to get amazing offers";
    public static final String ACCOUNT_CONFIRMATION_SUBJECT = "WELCOME TO ZELIA";
    public static final int LENGTH_OF_OTP = 6;
    public static final int OTP_EXPIRE_DURATION = 4;
    public static final String INVALID_OTP_CODE = "010";
    public static final String INVALID_OTP_MESSAGE = "Invalid Otp. Please try again. Note! You have only 3 attempts";
    public static final String OTP_SENT_CODE = "011";
    public static final String OTP_SENT_MESSAGE = "Otp has been successfully sent";
    public static final String PASSWORD_RESET_SUBJECT = "PASSWORD RESET!!!";
    public static final String PASSWORD_RESET_CODE = "012";
    public static final String PASSWORD_RESET_MESSAGE = "Password reset link has been sent";
    public static final String PASSWORD_TOKEN_EXPIRED_CODE = "013";
    public static final String PASSWORD_TOKEN_EXPIRED_MESSAGE = "Password Reset Token is expired";
    public static final String PASSWORD_RESET_SUCCESS_CODE = "014";
    public static final String PASSWORD_RESET_SUCCESS_MESSAGE = "Password Reset Successfully!";
    public static final String PASSWORD_INVALID_CODE = "015";
    public static final String PASSWORD_INVALID_MESSAGE = "Your password is Invalid. Ensure Password is not less than 8 characters and not more than 20 characters. Also ensure password contains letters, numbers and at least one symbol";
    public static final String IDENTITY_VERIFY_SUCCESS_CODE = "016";
    public static final String IDENTITY_VERIFY_SUCCESS_MESSAGE = "NIN has been successfully verified";
    public static final String INVALID_ID_CODE = "017";
    public static final String INVALID_ID_MESSAGE = "Invalid ID type";
    public static final String BVN_VALID_CODE = "018";
    public static final String BVN_VALID_MESSAGE = "Bvn has been successfully validated";
    public static final String BVN_INVALID_CODE = "019";
    public static final String BVN_INVALID_MESSAGE = "Invalid Bvn";
    public static final String OTP_VALIDATED_CODE = "020";
    public static final String OPT_VALIDATED_MESSAGE = "Otp has been successfully validated";
    public static final String BIOMETRIC_INFO_SAVED_CODE = "021";
    public static final String BIOMETRIC_INFO_SAVED_MESSAGE = "Biometric info has been saved";
    public static final String UNDERAGE_CODE = "022";
    public static final String UNDERAGE_MESSAGE = "You must be at least 18 years old";

    public static final String EMAIL_SENDER_ID = "majibade5@gmail.com";

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

    @Bean
    public String generateOtp(){
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        int count = 0;
        while (count < LENGTH_OF_OTP){
            otp.append(random.nextInt(10));
            ++count;
        }
        return otp.toString();
    }


    public String generateReferralCode(){
        StringBuilder password = new StringBuilder();
        int count = 0;
        Random randomPassword = new Random();
        while (count < LENGTH_OF_PASSWORD){
            password.append((char) randomPassword.nextInt(33, 122));
            count++;
        }
        return password.toString();
    }

    public Boolean verifyPasswordLength(String password){
        return password.length() >= 7;
    }

    public Boolean isPasswordValid(String password){
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,20}$";
        Pattern pattern = Pattern.compile(regex);
        if (password == null){
            return false;
        }
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public Boolean isPinValid(String pin, int yearOfBirth){
        return pin.length() == 4 && !pin.equals("1234") && !pin.equals("0000") && !pin.equals(String.valueOf(yearOfBirth));
    }

    public String encode(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isLowerCase(ch) ? 'a' : 'A';
                result.append((char) ((ch - base + shift) % 26 + base));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    // Function to decode text using Caesar Cipher
    public String decode(String text, int shift) {
        return encode(text, 26 - shift);
    }

    public static void main(String[] args) {

    }


    public String decodePassword(String encodedPassword){
        return Arrays.toString(Base64.getDecoder().decode(encodedPassword));
    }

}
