package services;

import entities.TypeUtilisateur;
import entities.Utilisateur;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IUtilisateur<U> {
    void ajouterUtilisateur(U utilisateur)throws SQLException;
    Utilisateur trouverUtilisateurParEmail(String email);
    int loginUtilisateur(String email, String motDePasse)throws SQLException;
    public void modifierUtilisateur(U t) throws SQLException;
    public void supprimerUtilisateur(U t )throws SQLException;
    Utilisateur trouverUtilisateurParCin(int cin);

    Map<TypeUtilisateur, Long> calculerStatistiquesUtilisateurs();
}