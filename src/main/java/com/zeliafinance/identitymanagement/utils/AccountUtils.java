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
    public static final String OTP_VALIDATED_MESSAGE = "Otp has been successfully validated";
    public static final String BIOMETRIC_INFO_SAVED_CODE = "021";
    public static final String BIOMETRIC_INFO_SAVED_MESSAGE = "Biometric info has been saved";
    public static final String UNDERAGE_CODE = "022";
    public static final String UNDERAGE_MESSAGE = "You must be at least 18 years old";
    public static final String OTP_EXPIRED_CODE = "023";
    public static final String OTP_EXPIRED_MESSAGE = "Otp is expired! Kindly resend another otp";
    public static final String EMAIL_SENDER_ID = "majibade5@gmail.com";
    public static final String JWT_SECRET="RPTyyaBeHl04wqPFd86G/tssX+pTxPq6HHCa2QnCOAU=";
    public static final long JWT_EXPIRATION=86400000;
    public static final String SUCCESS_CODE = "024";
    public static final String SUCCESS_MESSAGE = "Success";
    public static final String INVALID_PIN_CODE = "025";
    public static final String INVALID_PIN_MESSAGE = "Your pin is invalid. Ensure your pin is NOT 1234 nor your year of birth nor 0000";
    public static final String PIN_DISPARITY_CODE = "026";
    public static final String PIN_DISPARITY_MESSAGE = "There's a disparity between your pin and confirm pin";
    public static final String IDENTITY_VERIFICATION_FAIL_CODE = "027";
    public static final String IDENTITY_VERIFICATION_FAIL_MESSAGE = "Identity Mismatch";
    public static final String INVALID_CREDENTIALS_CODE = "028";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Username or password invalid";
    public static final String EMAIL_NOT_VERIFIED_CODE = "029";
    public static final String EMAIL_NOT_VERIFIED_MESSAGE = "User Email has not been verified";
    public static final String RESOURCE_NOT_FOUND_CODE = "030";
    public static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource with given lookupCode does not exist";
    public static final String PIN_SETUP_SUCCESS_CODE = "031";
    public static final String PIN_SETUP_SUCCESS_MESSAGE = "Pin has been successfully set up";
    public static final String PIN_VALIDATED_CODE = "032";
    public static final String PIN_VALIDATED_MESSAGE = "PIN VALIDATED SUCCESSFULLY";
    public static final int BVN_LENGTH = 11;
    public static final String BVN_MISMATCH_CODE = "033";
    public static final String BVN_MISMATCH_MESSAGE = "BVN Mismatch";
    public static final String NIN_INVALID_CODE = "034";
    public static final String NIN_INVALID_MESSAGE = "Nin invalid";
    public static final String NIN_MISMATCH_CODE = "035";
    public static final String NIN_MISMATCH_MESSAGE = "Nin Mismatch";
    public static final String BUCKET_NAME = "zelia-file-storage";
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
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{6,20}$";
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
        AccountUtils accountUtils = new AccountUtils();

    }

    public String encodePin(String pin){
        return Base64.getEncoder().encodeToString(pin.getBytes());
    }

    public String decodePin(String pin){
        return new String(Base64.getDecoder().decode(pin));
    }


    public String decodePassword(String encodedPassword){
        return Arrays.toString(Base64.getDecoder().decode(encodedPassword));
    }

    public boolean validateBvnAndNin(String govtId){
        return govtId.startsWith("1234") && govtId.endsWith("02");
    }

}
