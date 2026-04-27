import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * SecurityUtils - Handles password hashing, salt generation, and OTP logic.
 */
public class SecurityUtils {

    /**
     * Hashes a password with a salt using SHA-256.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Generates a random salt.
     */
    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Verifies if the provided password matches the stored hash.
     */
    public static boolean verifyPassword(String password, String storedHash, String storedSalt) {
        if (storedSalt == null || storedSalt.isEmpty()) return false;
        String newHash = hashPassword(password, storedSalt);
        return newHash.equals(storedHash);
    }

    /**
     * Generates a simple 6-digit OTP.
     */
    public static String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
