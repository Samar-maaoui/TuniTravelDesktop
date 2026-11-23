package utils;

import java.util.UUID;

public class ReservationUtils {
    // Générer une clé unique pour la réservation
    public static String generateUniqueKey() {
        // Générer un UUID aléatoire unique
        return UUID.randomUUID().toString();
    }
}
