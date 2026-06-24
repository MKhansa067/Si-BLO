package com.siblo.rent.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Menyimpan OTP sementara di memory (bukan database).
 * OTP valid selama 5 menit.
 * Cocok untuk testing/dummy di localhost.
 */
@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final SecureRandom random = new SecureRandom();

    // key = email, value = [kode OTP, waktu expired]
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    public String generateOtp(String email) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        String code = otp.toString();
        otpStore.put(email, new OtpData(code, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        return code;
    }

    public boolean verifyOtp(String email, String code) {
        OtpData data = otpStore.get(email);
        if (data == null) return false;
        if (LocalDateTime.now().isAfter(data.expiredAt)) {
            otpStore.remove(email);
            return false;
        }
        if (!data.code.equals(code)) return false;
        otpStore.remove(email);
        return true;
    }

    private static class OtpData {
        String code;
        LocalDateTime expiredAt;

        OtpData(String code, LocalDateTime expiredAt) {
            this.code = code;
            this.expiredAt = expiredAt;
        }
    }
}