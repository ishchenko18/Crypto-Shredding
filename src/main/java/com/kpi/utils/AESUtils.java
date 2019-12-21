package com.kpi.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESUtils {

    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);

        return keyGenerator.generateKey();
    }

    public static String encrypt(String stringToEncrypt, SecretKey secretKey) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return Base64.getEncoder().encodeToString(cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String stringToDecrypt, SecretKey secretKey) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(Base64.getDecoder().decode(stringToDecrypt)));
    }
}
