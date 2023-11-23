package com.zeliafinance.identitymanagement.thirdpartyapis.bani.utils;

import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
@AllArgsConstructor
public class SignatureGenerator {

    private AccountUtils accountUtils;

    public static String generateSignature(String secret, String digest) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(keySpec);
        byte[] signatureBytes = hmacSha256.doFinal(digest.getBytes(StandardCharsets.UTF_8));
        StringBuilder signatureBuilder = new StringBuilder();
        for (byte b : signatureBytes) {
            String hex = String.format("%02x", b);
            signatureBuilder.append(hex);
        }
        return signatureBuilder.toString();
    }

    public static String encryptWebHookData(String data, String secret){
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
        String merchantKey = "KZFeMtICCss8pdxlRbMgtg";
        String tribeAccountRef = "BN-hq0pn40mpzjgrrwzkxfsbvbp3m";
        String publicKey = "pub_test_WT8EKBTG4D9CQEDC27W2Z";
        String digest = tribeAccountRef + publicKey;
        String signature = generateSignature(merchantKey, digest);
        System.out.println("Signature :" + signature);
    }
}
