package edu.upc.labpilot.config;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class MfaUtil {
    
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP = 30; // segundos
    
    public String generarSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }
    
    public boolean verificarCodigo(String secret, String code) {
        if (secret == null || code == null) return false;
        
        long timeIndex = System.currentTimeMillis() / 1000 / TIME_STEP;
        
        // Verificar código actual y códigos cercanos (para sincronización)
        for (int i = -1; i <= 1; i++) {
            String testCode = generarCodigo(secret, timeIndex + i);
            if (testCode.equals(code)) {
                return true;
            }
        }
        return false;
    }
    
    private String generarCodigo(String secret, long timeIndex) {
        try {
            Base32 base32 = new Base32();
            byte[] key = base32.decode(secret);
            byte[] data = new byte[8];
            
            for (int i = 8; i-- > 0; timeIndex >>>= 8) {
                data[i] = (byte) timeIndex;
            }
            
            SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);
            
            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;
            
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= Math.pow(10, CODE_DIGITS);
            
            return String.format("%0" + CODE_DIGITS + "d", truncatedHash);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generando código MFA", e);
        }
    }
    
    public String generarCodigoSMS() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
