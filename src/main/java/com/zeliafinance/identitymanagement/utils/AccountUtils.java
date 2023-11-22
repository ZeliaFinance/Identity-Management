package com.zeliafinance.identitymanagement.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AccountUtils {
    public static final String BVN_EXISTS_MESSAGE = "User with this bvn already exists";
    public static final String EMAIL_EXISTS_MESSAGE = "User with this email address exists";
    public static final String IDENTITY_EXISTS_MESSAGE = "User with this identity already exists";
    public static final int LENGTH_OF_ACCOUNT_NUMBER = 10;
    public static final int LENGTH_OF_PASSWORD = 8;
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "User Account has been successfully created!";
    public static final String ACCOUNT_CREATION_ALERT_SUBJECT = "ACCOUNT CREATION ALERT!!!";
    public static final String USER_NOT_EXIST_MESSAGE = "Sign up first. Then proceed to update your profile";
    public static final String USER_ROLE_SET_MESSAGE = "User role has been successfully updated";
    public static final String LOGIN_SUCCESS_MESSAGE = "Successfully Logged in.";
    public static final String ROLE_UPDATE_SUCCESS_MESSAGE = "User role updated!";
    public static final String PASSWORD_INCORRECT_MESSAGE= "Disparity in Password and Confirm Password fields";
    public static final String ACCOUNT_CONFIRMATION_MESSAGE = "Congrats! Your account has been successfully created. Now click on this link to get amazing offers";
    public static final String ACCOUNT_CONFIRMATION_SUBJECT = "WELCOME TO ZELIA";
    public static final int LENGTH_OF_OTP = 6;
    public static final String INVALID_OTP_MESSAGE = "Invalid Otp. Please try again. Note! You have only 3 attempts";
    public static final String OTP_SENT_MESSAGE = "Otp has been successfully sent";
    public static final String PASSWORD_RESET_MESSAGE = "Password reset link has been sent";
    public static final String PASSWORD_TOKEN_EXPIRED_MESSAGE = "Password Reset Token is expired";
    public static final String PASSWORD_RESET_SUCCESS_MESSAGE = "Password Reset Successfully!";
    public static final String PASSWORD_INVALID_MESSAGE = "Your password is Invalid. Ensure Password is not less than 8 characters and not more than 20 characters. Also ensure password contains letters, numbers and at least one symbol";
    public static final String IDENTITY_VERIFY_SUCCESS_MESSAGE = "NIN has been successfully verified";
    public static final String INVALID_ID_MESSAGE = "Invalid ID type";
    public static final String BVN_VALID_MESSAGE = "Bvn has been successfully validated";
    public static final String BVN_INVALID_MESSAGE = "Invalid Bvn";
    public static final String OTP_VALIDATED_MESSAGE = "Otp has been successfully validated";
    public static final String BIOMETRIC_INFO_SAVED_MESSAGE = "Biometric info has been saved";
    public static final String UNDERAGE_MESSAGE = "You must be at least 18 years old";
    public static final String OTP_EXPIRED_MESSAGE = "Otp is expired! Kindly resend another otp";
    public static final String JWT_SECRET="RPTyyaBeHl04wqPFd86G/tssX+pTxPq6HHCa2QnCOAU=";
    public static final long JWT_EXPIRATION=86400000;
    public static final String SUCCESS_MESSAGE = "Success";
    public static final String INVALID_PIN_MESSAGE = "Your pin is invalid. Ensure your pin is NOT 1234 nor your year of birth nor 0000";
    public static final String PIN_DISPARITY_MESSAGE = "There's a disparity between your pin and confirm pin";
    public static final String IDENTITY_VERIFICATION_FAIL_MESSAGE = "Identity Mismatch";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Username or password invalid";
    public static final String EMAIL_NOT_VERIFIED_MESSAGE = "User Email has not been verified";
    public static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource with given lookupCode does not exist";
    public static final String PIN_SETUP_SUCCESS_MESSAGE = "Pin has been successfully set up";
    public static final String PIN_VALIDATED_MESSAGE = "PIN VALIDATED SUCCESSFULLY";
    public static final int BVN_LENGTH = 11;
    public static final String BVN_MISMATCH_MESSAGE = "BVN Mismatch";
    public static final String NIN_INVALID_MESSAGE = "Nin invalid";
    public static final String NIN_MISMATCH_MESSAGE = "Nin Mismatch";
    public static final String BUCKET_NAME = "zelia-file-storage";
    public static final String AWS_FILE_BASE_URL = "https://zelia-file-storage.s3.amazonaws.com";
    public static final String INVALID_TOKEN_MESSAGE = "Invalid Token";
    public static final String NON_UNIQUE_PHONE_NUMBER_MESSAGE = "Phone Number must be unique";
    public static final String PROFILE_UPDATE_SUCCESS = "Profile has been successfully updated";
    public static final String NON_UNIQUE_BVN_MESSAGE = "Bvn must be unique";
    public static final String PENDING_LOAN_MESSAGE = "You have a pending loan";
    public static final String LOAN_APPLICATION_SUCCESS = "Loan Application has been submitted for processing";
    public static final String PENDING_LOAN_EXISTS = "You have an unpaid loan still running";
    public static final String LOAN_NOT_FOUND = "Loan not found";
    public static final String NON_ADMIN_LOGIN = "No User Access";
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
        String regex = "^(.)\\1*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pin);
        return pin.length() == 4 && !pin.equals("1234") && !matcher.matches() && !pin.equals(String.valueOf(yearOfBirth));
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

    public static String decodeKeys(String text, int shift) {
        AccountUtils accountUtils = new AccountUtils();
        return accountUtils.encode(text, 26 - shift);
    }

    public static void main(String[] args) {
        AccountUtils accountUtils = new AccountUtils();
//        System.out.println(accountUtils.decode("IOZSXEN_WHVW-8056gd94h6500ehg368dh7d98iie428f-A"
//                , 3));
//        System.out.println(accountUtils.encode("ef3e5df0a128aa43ff4f4115683f9b065009dfeaa18c43c25b5016d281129683", 5));

        String prefix = "+234";
        String suffix = "08139148965";
        String result = prefix + suffix.substring(1);
        System.out.println(result);
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

    public String generateLoanRefNo(){
        String prefix = "ZLF";
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        while(count < 12){
            Random random = new Random();
            stringBuilder.append(random.nextInt(10));
            count++;
            if (count % 4 == 0 && stringBuilder.length() < 14){
                stringBuilder.append("-");
            }
        }
        return prefix + "-" + stringBuilder;
    }

    public String transactionRef(){
        String currentTime = LocalDateTime.now().toString();
        StringBuilder transactionRef = new StringBuilder();
        for (int i=0; i<currentTime.length(); i++){
            if (currentTime.charAt(i) == 'T' || currentTime.charAt(i) == '-' || currentTime.charAt(i) == ':' || currentTime.charAt(i) == '.'){
                continue;
            }
            transactionRef.append(currentTime.charAt(i));
        }
        System.out.println(transactionRef.length());
        return transactionRef.toString();
    }

}
