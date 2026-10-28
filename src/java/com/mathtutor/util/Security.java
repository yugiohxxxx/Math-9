package com.mathtutor.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Security {
    /** SHA-256 dạng HEX viết hoa (khớp CONVERT(VARCHAR(64),HASHBYTES('SHA2_256',..),2) trong SQL Server). */
    public static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
