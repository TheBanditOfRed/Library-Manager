package core;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages encryption and decryption operations for sensitive data in the library management system.
 * Uses AES-GCM encryption with PBKDF2 key derivation for secure data protection.
 */
public class SecurityManager {
    private static final Logger logger = Logger.getLogger(SecurityManager.class.getName());

    /** Constants for AES-GCM encryption parameters */
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;

    /**
     * Encrypts a string using AES-GCM with a password-derived key.
     * The encryption process includes:
     * 1. Generating a random salt for key derivation
     * 2. Deriving an AES key from the password using PBKDF2
     * 3. Generating a random initialization vector (IV)
     * 4. Encrypting the plaintext with AES-GCM
     * 5. Combining salt, IV, and ciphertext into a single Base64-encoded string
     *
     * @param plaintext The text to encrypt
     * @param password The password to derive the encryption key from
     * @return A Base64-encoded string containing the salt, IV, and encrypted data
     * @throws RuntimeException if encryption fails
     */
    public static String encrypt(String plaintext, String password) {
        logger.fine("Encryption operation initiated"); // Fine level to avoid password leaks
        try {
            byte[] salt = new byte[SALT_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            SecretKey key = getKeyFromPassword(password, salt);

            byte[] iv = new byte[GCM_IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            cipher.updateAAD(salt);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[salt.length + iv.length + ciphertext.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(ciphertext, 0, combined, salt.length + iv.length, ciphertext.length);
            logger.fine("Encryption operation completed successfully");
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Encryption operation failed", e);
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypts a string that was encrypted with the encrypt() method.
     * The decryption process includes:
     * 1. Base64-decoding the input string
     * 2. Extracting the salt, IV, and encrypted data
     * 3. Deriving the key from the password using the extracted salt
     * 4. Decrypting the data using AES-GCM
     *
     * @param ciphertext The Base64-encoded encrypted string (containing salt, IV, and encrypted data)
     * @param password The password to derive the decryption key from
     * @return The decrypted plaintext string
     * @throws RuntimeException if decryption fails (e.g., wrong password or tampered data)
     */
    public static String decrypt(String ciphertext, String password) {
        logger.fine("Decryption operation initiated");
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);

            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - salt.length - iv.length];

            System.arraycopy(decoded, 0, salt, 0, salt.length);
            System.arraycopy(decoded, salt.length, iv, 0, iv.length);
            System.arraycopy(decoded, salt.length + iv.length, encrypted, 0, encrypted.length);

            SecretKey key = getKeyFromPassword(password, salt);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            cipher.updateAAD(salt);

            byte[] decrypted = cipher.doFinal(encrypted);
            logger.fine("Decryption operation completed successfully");
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Decryption operation failed - possibly wrong password", e);
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    /**
     * Derives a cryptographic key from a password using PBKDF2 with HMAC-SHA256.
     * Uses the provided salt and a fixed iteration count (65,536 iterations) to derive a 256-bit AES key.
     *
     * @param password The password to derive the key from
     * @param salt The cryptographic salt to use for key derivation (must be at least 16 bytes)
     * @return A SecretKey object suitable for AES encryption/decryption operations
     * @throws Exception if key derivation fails due to invalid parameters or cryptographic errors
     */
    private static SecretKey getKeyFromPassword(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}