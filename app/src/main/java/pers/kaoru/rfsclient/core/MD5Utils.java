package pers.kaoru.rfsclient.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    public static String GenerateMD5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            return toHexString(messageDigest.digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toHexString(b & 0xff));
        }
        return sb.toString();
    }
}
