package utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Méthode pour hacher un mot de passe
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12)); // Génère un hash avec un sel sécurisé
    }

    // Méthode pour vérifier si un mot de passe correspond au hash stocké
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (hashedPassword != null && hashedPassword.startsWith("$2y$")) {
            System.out.println("✔ Conversion du hash $2y$ → $2a$ pour compatibilité Java.");
            hashedPassword = "$2a$" + hashedPassword.substring(4);
        }
        return BCrypt.checkpw(password, hashedPassword);
    }

}
