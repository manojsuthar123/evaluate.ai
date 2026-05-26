package com.evaluate.ai.langchain.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class QuestionUtils {

    public static String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
