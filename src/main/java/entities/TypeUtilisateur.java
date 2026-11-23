package entities;

public enum TypeUtilisateur {
    NORMAL, VIP, ADMIN;

    // Méthode de conversion insensible à la casse
    public static TypeUtilisateur fromString(String value) {
        if (value != null) {
            // Convertir la chaîne de caractères en majuscule et renvoyer le type correspondant
            switch (value.trim().toUpperCase()) {
                case "ADMIN":
                    return ADMIN;
                case "VIP":
                    return VIP;
                case "NORMAL":
                    return NORMAL;
                default:
                    throw new IllegalArgumentException("Valeur inconnue : " + value);
            }
        }
        throw new IllegalArgumentException("Valeur null pour TypeUtilisateur");
    }
}
