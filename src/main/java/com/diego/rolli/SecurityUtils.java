package com.diego.rolli;

import android.text.TextUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SecurityUtils {

    public static final int MAX_NAME_LENGTH = 60;
    public static final int MAX_ROLE_OR_CAREER_LENGTH = 50;
    public static final int MAX_CONTROL_LENGTH = 20;
    public static final int MAX_CLASSROOM_LENGTH = 60;
    public static final int PHONE_LENGTH = 10;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            "(?i)(--|;|/\\*|\\*/|'|\"|`|\\\\|<|>|\\$|\\{|\\}|\\b(union\\s+select|drop\\s+table|insert\\s+into|delete\\s+from|update\\s+\\w+\\s+set|exec\\s*\\(|xp_cmdshell|script)\\b)"
    );
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F\\u200B-\\u200F\\u202A-\\u202E]");
    private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HUMAN_NAME_PATTERN = Pattern.compile("^[\\p{L}0-9 .,'-]{2,60}$");
    private static final Pattern GENERAL_TEXT_PATTERN = Pattern.compile("^[\\p{L}0-9 .,#()/-]{1,60}$");
    private static final Pattern ALPHANUMERIC_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{0,20}$");
    private static final Pattern PICKUP_CODE_PATTERN = Pattern.compile("^\\d{4}$");

    private SecurityUtils() {
    }

    public static String sanitizeText(String raw, int maxLength) {
        if (raw == null) {
            return "";
        }
        String cleaned = CONTROL_CHARS.matcher(raw).replaceAll(" ");
        cleaned = MULTI_WHITESPACE.matcher(cleaned).replaceAll(" ").trim();
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength).trim();
        }
        return cleaned;
    }

    public static String sanitizeDigitsOnly(String raw, int maxLength) {
        if (raw == null) {
            return "";
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() > maxLength) {
            digits = digits.substring(0, maxLength);
        }
        return digits;
    }

    public static boolean containsDangerousPattern(String input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        return DANGEROUS_PATTERN.matcher(input).find();
    }

    public static boolean isValidHumanName(String name) {
        if (TextUtils.isEmpty(name) || containsDangerousPattern(name)) {
            return false;
        }
        return HUMAN_NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidCareerOrRole(String text) {
        if (TextUtils.isEmpty(text) || text.length() > MAX_ROLE_OR_CAREER_LENGTH || containsDangerousPattern(text)) {
            return false;
        }
        return GENERAL_TEXT_PATTERN.matcher(text).matches();
    }

    public static boolean isValidClassroom(String classroom) {
        if (TextUtils.isEmpty(classroom) || classroom.length() > MAX_CLASSROOM_LENGTH || containsDangerousPattern(classroom)) {
            return false;
        }
        return GENERAL_TEXT_PATTERN.matcher(classroom).matches();
    }

    public static boolean isValidOptionalControlNumber(String controlNumber) {
        if (TextUtils.isEmpty(controlNumber)) {
            return true;
        }
        if (controlNumber.length() > MAX_CONTROL_LENGTH || containsDangerousPattern(controlNumber)) {
            return false;
        }
        return ALPHANUMERIC_ID_PATTERN.matcher(controlNumber).matches();
    }

    public static boolean isValidPhone10(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() == PHONE_LENGTH && TextUtils.isDigitsOnly(phone);
    }

    public static boolean isValidPickupCode(String code) {
        return !TextUtils.isEmpty(code) && PICKUP_CODE_PATTERN.matcher(code).matches();
    }

    public static String escapeSqlLikeLiteral(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static String generateOrderId() {
        return UUID.randomUUID().toString();
    }

    public static String generatePickupCode() {
        int code = 1000 + SECURE_RANDOM.nextInt(9000);
        return String.format(Locale.US, "%04d", code);
    }

    public static boolean verifyAdminPin(String candidatePin) {
        if (TextUtils.isEmpty(candidatePin) || TextUtils.isEmpty(BuildConfig.ADMIN_PIN_HASH)) {
            return false;
        }
        String saltedInput = BuildConfig.ADMIN_PIN_SALT + ":" + candidatePin.trim();
        byte[] computed = sha256Bytes(saltedInput);
        byte[] expected = hexToBytes(BuildConfig.ADMIN_PIN_HASH);
        if (computed == null || expected == null || computed.length != expected.length) {
            return false;
        }
        return MessageDigest.isEqual(computed, expected);
    }

    private static byte[] sha256Bytes(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static byte[] hexToBytes(String hex) {
        String normalized = hex.trim();
        if (normalized.length() % 2 != 0) {
            return null;
        }
        int len = normalized.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(normalized.charAt(i), 16);
            int low = Character.digit(normalized.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                return null;
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }
}
