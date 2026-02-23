package com.tgrah.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

public class TotpGenerator {

    public static String generateTotp(String base32Secret) {

        long time = System.currentTimeMillis() / 1000L / 30;

        byte[] key = base32Decode(base32Secret);

        byte[] msg = longToBytes(time);

        byte[] hash = hash(key, msg);

        int offset = hash[hash.length - 1] & 0x0F;

        int binary
                = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % 1_000_000;

        return String.format("%06d", otp);
    }

    private static byte[] base32Decode(String secret) {
        Base32 base32 = new Base32();
        return base32.decode(secret.getBytes());
    }

    private static byte[] hash(byte[] key, byte[] msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA1");
            mac.init(keySpec);
            return mac.doFinal(msg);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static byte[] longToBytes(long val) {

        byte[] result = new byte[8];

        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (val & 0xFF);
            val >>= 8;
        }

        return result;
    }
}
