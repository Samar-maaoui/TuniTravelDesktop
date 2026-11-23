package controller;

import entities.Evenement;
import entities.Utilisateur;
import entities.reservationEvenement;
import services.ServiceReservationEvenement;
import services.ServiceEvenement;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import services.UtilisateurService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static services.Payment.initiatePayment;

public class CreerReservationController {
    private int idEvenement;
    private int capacite;
    private int idUtilisateur;

    @FXML
    private Spinner<Integer> spinnerNbPlaces;

    @FXML
    private Button btnReserver;

    private ServiceReservationEvenement serviceReservation = new ServiceReservationEvenement();

    public void setIdEvenement(int idEvenement, int capacite, int idUtilisateur) {
        this.idEvenement = idEvenement;
        this.capacite = capacite;
        this.idUtilisateur = SessionManager.getUtilisateur().getIdUtilisateur();

        spinnerNbPlaces.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, capacite, 1));
    }

    @FXML
    private void reserverEvenement() throws SQLException {
        int nbPlaces = spinnerNbPlaces.getValue();

        if (nbPlaces > capacite) {
            showAlert("Erreur", "Le nombre de places ne peut pas dépasser " + capacite);
            return;
        }

        reservationEvenement reservation = new reservationEvenement(0, idEvenement, idUtilisateur, nbPlaces, LocalDateTime.now());

        boolean success = serviceReservation.ajouterReservation(reservation);

        if (success) {
            handlePayment(idEvenement, idUtilisateur, nbPlaces);

            ((Stage) btnReserver.getScene().getWindow()).close();
        } else {
            showAlert("Erreur", "Échec de la réservation.");
        }
    }

    private void handlePayment(int idEvenement, int idUtilisateur, int nbPlaces) throws SQLException {
        System.out.println("Initiating payment for event ID: " + idEvenement);
        UtilisateurService i=new UtilisateurService();
        Evenement evenement = ServiceEvenement.getEvenementById(idEvenement);

        List<Utilisateur> utilisateurs = i.chargerTousLesUtilisateurs_mahdi();
        Utilisateur utilisateur = null;

        for (Utilisateur user : utilisateurs) {
            if (user.getIdUtilisateur() == idUtilisateur) {
                utilisateur = user;
                break;
            }
        }

        if (evenement != null && utilisateur != null) {
            initiatePayment(utilisateur, evenement, nbPlaces);
        } else {
            showAlert("Erreur", "Utilisateur ou événement introuvable.");
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
