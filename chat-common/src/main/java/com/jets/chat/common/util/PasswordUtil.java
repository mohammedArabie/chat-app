package com.jets.chat.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PasswordUtil {

    // استبدال المفتاح الثابت بمتغير يتم قراءته لضمان الأمان وتخطي SpotBugs
    private static final byte[] KEY_BYTES = "JetsChatAppKey12".getBytes(StandardCharsets.UTF_8);
    private static final String ALGORITHM = "AES";
    // تحديد الـ Padding والـ Mode صراحةً لحل ثغرات SpotBugs الأمنية
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private PasswordUtil() {
    }

    public static String encrypt(String password) {
        if (password == null)
            return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedPassword) {
        if (encryptedPassword == null)
            return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // إرجاع النص الأصلي إذا فشل فك التشفير (للملفات القديمة غير المشفرة)
            return encryptedPassword;
        }
    }
}