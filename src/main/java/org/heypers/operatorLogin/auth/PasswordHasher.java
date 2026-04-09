package org.heypers.operatorLogin.auth;

import java.security.MessageDigest;
import java.util.Base64;


public class PasswordHasher {


    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return "sha:" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return input;
        }
    }


    public static boolean matches(String raw, String stored) {
        if (!stored.startsWith("sha:")) return raw.equals(stored);

        return stored.equals(hash(raw));
    }
}