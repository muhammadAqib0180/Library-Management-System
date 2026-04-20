package util;

import java.security.SecureRandom;

/** Generates & validates 4-digit OTPs for book handover (Sprint 3 — US-1). */
public final class OtpService {
    private static final SecureRandom RNG = new SecureRandom();

    private OtpService() {}

    /** @return a 4-digit numeric OTP as a String (e.g. "0427"). */
    public static String generate() {
        int n = RNG.nextInt(10_000);
        return String.format("%04d", n);
    }

    public static boolean verify(String expected, String entered) {
        if (expected == null || entered == null) return false;
        return expected.trim().equals(entered.trim());
    }
}
