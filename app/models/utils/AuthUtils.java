package models.utils;

import models.exception.UndefinedSaltException;
import play.Play;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AuthUtils {

    private static String SALT_PREFIX = null;
    private static String SALT_SUFFIX = null;

    public static void configureSalt() {
        SALT_PREFIX = Play.configuration.getProperty("Config.salt.prefix");
        SALT_SUFFIX = Play.configuration.getProperty("Config.salt.suffix");
        if (SALT_PREFIX == null || SALT_SUFFIX == null) {
            throw new UndefinedSaltException("Undefined salt's prefix and suffix, you must define these at application.conf ('Config.salt.prefix' and 'Config.salt.suffix')");
        }
    }

    public static String generateToken(int length) {
        String alphanum = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRTUVWXYZ2346789";
        SecureRandom generator = new SecureRandom();
        char[] token = new char[length];
        for(int i=0; i< length; i++) {
            token[i] = alphanum.charAt(generator.nextInt(alphanum.length()));
            
        }
        return new String(token);    
    }

    /**
     *
     * @param input
     * @return a 64 characters long hex representation of the SHA-256 of the input
     */
    public static String hashId(String input) {
        input = SALT_PREFIX + input + SALT_SUFFIX;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes());
            byte[] hash = md.digest();
            StringBuilder hashString = new StringBuilder();
            for(int i=0; i< hash.length; i++) {
                int b = hash[i] & 0x00FF;
                String tmp = Integer.toHexString(b);
                if (tmp.length() ==1) {
                    tmp = "0" + tmp;
                }
                hashString.append(tmp);
            }
            return(hashString.toString());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Applies the same hash function as hashId (actually calls the same implementation), but should be used with
     * something that changes between users to be used as a unique salt.
     * @param password
     * @param salt
     * @return a 64 characters long hex representation of the SHA-256 of the input
     */
    public static String hashPassword(String password, String salt) {
        return hashId(salt+":"+password);
    }

}
