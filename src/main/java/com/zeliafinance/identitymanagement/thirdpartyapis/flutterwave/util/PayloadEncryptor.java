package com.zeliafinance.identitymanagement.thirdpartyapis.flutterwave.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@NoArgsConstructor
public class PayloadEncryptor {
    private static final String ALGORITHM = "DESede";
    private static final String TRANSFORMATION = "DESede/ECB/PKCS5Padding";

    public String TripleDESEncrypt(String data, String encryptionKey){
        final String defaultString = "";
        if (data == null || encryptionKey == null){
            return defaultString;
        }
        try {
            final byte[] encryptionKeyBytes = encryptionKey.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(encryptionKeyBytes, ALGORITHM);
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            final byte[] dataBytes = data.getBytes();
            byte[] encryptedValue = cipher.doFinal(dataBytes);
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
