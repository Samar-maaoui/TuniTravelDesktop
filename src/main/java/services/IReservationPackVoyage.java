package services;

import entities.ReservationPackVoyage;
import java.sql.SQLException;
import java.util.List;

public interface IReservationPackVoyage {

    // Ajouter une réservation de pack de voyage
    void ajouterReservation(ReservationPackVoyage reservation) throws SQLException;

    // Modifier une réservation de pack de voyage
    void modifierReservation(ReservationPackVoyage reservation) throws SQLException;

    // Supprimer une réservation de pack de voyage
    void supprimerReservation(int reservationId) throws SQLException;

    // Lister toutes les réservations
    List<ReservationPackVoyage> listerReservations(int utilisateurId) throws SQLException;

    // Trouver une réservation par son ID
    ReservationPackVoyage trouverReservationParId(int reservationId) throws SQLException;

    // Lister les réservations d'un pack de voyage par l'ID du pack
    List<ReservationPackVoyage> listerReservationsParPackId(int packId) throws SQLException;
}
