package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.util;

import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
@Component
@RequiredArgsConstructor
public class PaystackSignatureGenerator {

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {

        String key = "sk_test_2419283ad6120923dd43e13f0b314ee61d107402";
        String rawJson = "{\"paystack\":\"request\",\"body\":\"to_string\"}";

        JSONObject body = new JSONObject(rawJson);
        String result = "";
        String HMAC_SHA512 = "HmacSHA512";
        String xpaystackSignature = ""; //put in the request's header value for x-paystack-signature

        byte [] byteKey = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
        Mac sha512_HMAC = Mac.getInstance(HMAC_SHA512);
        sha512_HMAC.init(keySpec);
        byte [] mac_data = sha512_HMAC.
                doFinal(body.toString().getBytes(StandardCharsets.UTF_8));

        result = DatatypeConverter.printHexBinary(mac_data);
        if(result.toLowerCase().equals(xpaystackSignature)) {
            // you can trust the event, it came from paystack
            // respond with the http 200 response immediately before attempting to process the response
            //retrieve the request body, and deliver value to the customer
        }else{
            // this isn't from Paystack, ignore it
        }
    }


}
