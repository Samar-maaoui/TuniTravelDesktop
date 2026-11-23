package controller;

import entities.Hotel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceHotel;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class modifier_hotelController {

    @FXML
    private TextField txtNommodifier, txtPrixmodifier, txtAdressemodifier, txtTelmodifier, txtEmailmodifier;
    @FXML
    private TextArea txtDescriptionmodifier;
    @FXML
    private ToggleButton btnDisponible;
    @FXML
    private Button bntmodifier1, btnAnnuler;

    private final ServiceHotel serviceHotel = ServiceHotel.getInstance();
    private Hotel hotelSelectionne;

    @FXML
    public void initialize() {
        // ✅ Gestion du ToggleButton (Rouge = Indisponible, Vert = Disponible)
        btnDisponible.setOnAction(event -> {
            if (btnDisponible.isSelected()) {
                btnDisponible.setText("Disponible");
                btnDisponible.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            } else {
                btnDisponible.setText("Indisponible");
                btnDisponible.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            }
        });
    }
    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow(); // Vous pouvez utiliser n'importe quel bouton
        stage.close(); // Ferme la fenêtre actuelle
    }
    public void loadHotelInfo(Hotel hotel) {
        if (hotel != null) {
            this.hotelSelectionne = hotel;
            txtNommodifier.setText(hotel.getNom());
            txtPrixmodifier.setText(String.valueOf(hotel.getPrix_nuit()));
            txtAdressemodifier.setText(hotel.getAdresse());
            txtDescriptionmodifier.setText(hotel.getDescription());
            txtTelmodifier.setText(hotel.getTel());
            txtEmailmodifier.setText(hotel.getEmail_contact());

            // ✅ Initialiser l'état du ToggleButton
            btnDisponible.setSelected(hotel.isEst_disponible());
            if (hotel.isEst_disponible()) {
                btnDisponible.setText("Disponible");
                btnDisponible.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            } else {
                btnDisponible.setText("Indisponible");
                btnDisponible.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            }
        }
    }

    @FXML
    private void modifierhotel1(ActionEvent event) {
        if (hotelSelectionne != null) {
            try {
                // Vérification du prix
                double prix;
                try {
                    prix = Double.parseDouble(txtPrixmodifier.getText().trim());
                    if (prix < 0) {
                        showAlert(Alert.AlertType.WARNING, "Prix invalide", "Le prix ne peut pas être négatif.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Prix invalide", "Veuillez entrer un prix valide.");
                    return;
                }

                // Vérification du numéro de téléphone (8 à 15 chiffres)
                if (!Pattern.matches("\\d{8,15}", txtTelmodifier.getText().trim())) {
                    showAlert(Alert.AlertType.WARNING, "Téléphone invalide", "Le numéro doit contenir entre 8 et 15 chiffres.");
                    return;
                }

                // Vérification de l'email
                if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", txtEmailmodifier.getText().trim())) {
                    showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez entrer une adresse email valide.");
                    return;
                }

                // Mise à jour de l'hôtel
                hotelSelectionne.setPrix_nuit(prix);
                hotelSelectionne.setAdresse(txtAdressemodifier.getText().trim());
                hotelSelectionne.setDescription(txtDescriptionmodifier.getText().trim());
                hotelSelectionne.setTel(txtTelmodifier.getText().trim());
                hotelSelectionne.setEmail_contact(txtEmailmodifier.getText().trim());
                hotelSelectionne.setEst_disponible(btnDisponible.isSelected());

                serviceHotel.modifierhotel(hotelSelectionne);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Hôtel modifié avec succès !");

                // ✅ Fermeture de la fenêtre après modification
                fermerFenetre();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Une erreur est survenue : " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void toggleDisponibilite(ActionEvent event) {
        if (btnDisponible.isSelected()) {
            btnDisponible.setText("Disponible");
            btnDisponible.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        } else {
            btnDisponible.setText("Indisponible");
            btnDisponible.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        }
    }

}
