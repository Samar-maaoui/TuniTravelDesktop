package services;

import entities.PackVoyage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IPackVoyage<T> {
    void ajouterPack(T pack)throws SQLException;
    public List<PackVoyage> listerPacks()throws SQLException;
    T trouverPackParNom(String nom)throws SQLException;
    public void modifierPack(PackVoyage pack)throws SQLException;

    public void supprimerPack(PackVoyage pack) throws SQLException;

    T trouverPackParId(int id)throws SQLException;; // ✅ Nouvelle méthode


    // ✅ Vérification des places restantes avant réservation
    boolean placesDisponibles(int packId)throws SQLException;;

    // ✅ Mise à jour du statut des packs complets
    void verifierStatutPack()throws SQLException;
}
