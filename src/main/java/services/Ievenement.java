package services;

import entities.Evenement;
import java.util.List;

public interface Ievenement {
    void ajouterEvenement(Evenement evenement);
    List<Evenement> getEvenements();
    void modifierEvenement(Evenement evenement);
    void supprimerEvenement(int idEvenement);
}
