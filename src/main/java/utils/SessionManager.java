package utils;
import entities.Utilisateur;

public class SessionManager {
    private static Utilisateur utilisateurConnecte = null;
    private static SessionManager instance;


    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Méthode pour définir l'utilisateur connecté
    public static void setUtilisateur(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    // Méthode pour récupérer l'utilisateur connecté
    public static Utilisateur getUtilisateur() {
        return utilisateurConnecte;
    }

    // Méthode pour vérifier si un utilisateur est connecté
    public static boolean isUserLoggedIn() {
        return utilisateurConnecte != null;
    }

    // Méthode pour déconnecter l'utilisateur
    public static void logout() {
        utilisateurConnecte = null;
    }
}
