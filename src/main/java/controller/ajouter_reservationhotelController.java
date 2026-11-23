package controller;

import entities.ReservationHotel;
import services.ServiceReservationHotel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import entities.Hotel;
import services.ServiceHotel;

public class ajouter_reservationhotelController {

    @FXML
    private DatePicker dpCheckIn, dpCheckOut;
    @FXML
    private ComboBox<String> cbTypeChambre;
    @FXML
    private Button btnAjouter, btnAnnuler;
    @FXML
    private Label lblPrix; // Label pour afficher le prix

    private final ServiceReservationHotel serviceReservationHotel = new ServiceReservationHotel();
    private int id_hotel; // L'ID sera défini via setId_hotel
    private double prixNuit; // Prix à stocker pour l'hôtel sélectionné
    public void setId_hotel(int id_hotel) {
        this.id_hotel = id_hotel;
        System.out.println("✅ Hôtel sélectionné pour réservation : ID = " + id_hotel);
    }
    public void setPrix(double prix) {
        lblPrix.setText("Prix : " + prix + " DT/nuit"); // Met à jour l'affichage du prix
    }
    @FXML
    public void initialize() {
        cbTypeChambre.getItems().addAll("Simple", "Double", "Suite");

        // Écoutez les changements de sélection dans le ComboBox
        cbTypeChambre.setOnAction(e -> updatePrix());
    }

    public void setId_hotel(int id_hotel, double prix) {
        this.id_hotel = id_hotel;
        this.prixNuit = prix; // Enregistrez le prix de l'hôtel
        System.out.println("✅ Hôtel sélectionné pour réservation : ID = " + id_hotel + ", Prix/Nuit = " + prixNuit);
    }
    // Méthode pour calculer et afficher le prix
    private void updatePrix() {
        String typeChambre = cbTypeChambre.getValue();
        double prixAffiche;

        if ("Simple".equals(typeChambre)) {
            prixAffiche = prixNuit; // Prix normal
        } else if ("Double".equals(typeChambre)) {
            prixAffiche = prixNuit * 1.5; // Ajustement pour Double
        } else if ("Suite".equals(typeChambre)) {
            prixAffiche = prixNuit * 2; // Ajustement pour Suite
        } else {
            lblPrix.setText("Sélectionnez un type de chambre.");
            return;
        }

        // Afficher le prix calculé
        lblPrix.setText("Prix : " + prixAffiche + " DT/nuit");
    }

    @FXML
    private void ajouterReservation(ActionEvent event) {
        if (id_hotel <= 0) {
            showAlert(Alert.AlertType.ERROR, "Aucun hôtel sélectionné !");
            return;
        }

        LocalDate checkInDate = dpCheckIn.getValue();
        LocalDate checkOutDate = dpCheckOut.getValue();
        String typeChambre = cbTypeChambre.getValue();

        if (checkInDate == null || checkOutDate == null || typeChambre == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs !");
            return;
        }

        if (checkOutDate.isAfter(checkInDate)) {
            showAlert(Alert.AlertType.WARNING, "La date de départ doit être avant la date d'arrivée !");
            return;
        }

        ReservationHotel reservation = new ReservationHotel(0, id_hotel, Date.valueOf(checkInDate), Date.valueOf(checkOutDate), typeChambre);
        try {
            serviceReservationHotel.ajouterReservation(reservation);
            showAlert(Alert.AlertType.INFORMATION, "Réservation ajoutée avec succès !");
            fermerFenetre();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ajout de la réservation : " + e.getMessage());
        }
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle("Information");
        alert.setHeaderText(null); // Pas de header
        alert.showAndWait(); // Affiche l'alerte et attend la fermeture
    }

}