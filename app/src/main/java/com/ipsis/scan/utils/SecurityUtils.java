package com.ipsis.scan.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by pobouteau on 9/28/16.
 */

public class SecurityUtils {

    private static final char[] CHARSET = "abcdefghijklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUWXYZ0123456789".toCharArray();

    public static String nonce() {
        Random random = new SecureRandom();
        char[] result = new char[40];

        for (int i = 0; i < result.length; i++) {
            int randomCharIndex = random.nextInt(CHARSET.length);
            result[i] = CHARSET[randomCharIndex];
        }
        return new String(result);
    }

    public static String identifier() {
        Random random = new SecureRandom();
        char[] result = new char[80];

        for (int i = 0; i < result.length; i++) {
            int randomCharIndex = random.nextInt(CHARSET.length);
            result[i] = CHARSET[randomCharIndex];
        }
        return new String(result);
    }

    public static String sha256(String text) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] data = digest.digest(text.getBytes(StandardCharsets.UTF_8));

        return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
    }
}
