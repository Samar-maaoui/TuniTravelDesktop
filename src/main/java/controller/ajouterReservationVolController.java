package controller;
import entities.vol;

import entities.reservationvol;
import services.c_reservationvol;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert.AlertType;
import java.sql.SQLException;

public class ajouterReservationVolController {

    @FXML
    private ComboBox<reservationvol.ClasseService> classeServiceComboBox;

    private vol selectedVol; // Objet vol sélectionné

    private final c_reservationvol reservationService = new c_reservationvol();

    @FXML
    public void initialize() {
        classeServiceComboBox.getItems().setAll(reservationvol.ClasseService.values());
    }

    // Méthode pour définir le vol sélectionné
    public void setVol(vol v) {
        this.selectedVol = v;
    }

    @FXML
    private void ajouterReservation() {
        try {
            if (selectedVol == null) {
                showAlert(AlertType.ERROR, "Erreur", "Aucun vol sélectionné.");
                return;
            }

            // Utilisez l'ID du vol sélectionné pour la réservation
            int idVol = selectedVol.getId_vol(); // Assurez-vous que 'getId()' est une méthode valide dans votre classe vol

            if (idVol <= 0) {
                showAlert(AlertType.ERROR, "Erreur", "Aucun vol trouvé.");
                return;
            }

            reservationvol.ClasseService classe = classeServiceComboBox.getValue();
            if (classe == null) {
                showAlert(AlertType.ERROR, "Erreur", "Veuillez sélectionner une classe.");
                return;
            }

            reservationvol reservation = new reservationvol(classe);
            reservation.setId_vol(idVol); // Assigner le vol sélectionné

            reservationService.ajouter(reservation);
            showAlert(AlertType.INFORMATION, "Succès", "Réservation ajoutée avec succès!");
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur SQL", "Impossible d'ajouter la réservation: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
