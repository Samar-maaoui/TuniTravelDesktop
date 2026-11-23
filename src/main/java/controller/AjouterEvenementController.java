package controller;

import entities.Evenement;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import services.ServiceEvenement;

import java.time.LocalDate;

public class AjouterEvenementController {
    @FXML private TextField txtNom;
    @FXML private TextField txtDescription;
    @FXML private TextField txtLieu;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtCapacite;
    @FXML private ComboBox<String> comboBoxType;
    @FXML private TextField txtOrganisateur;
    @FXML private TextField txtPrix;

    private static final String[] VALID_EVENT_TYPES = {"CONFÉRENCE", "SÉMINAIRE", "CONCERT", "SPORT"};
    @FXML
    private VBox formContainer;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (txtNom.getScene() != null) {
                txtNom.getScene().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }
        });

        comboBoxType.getItems().addAll(VALID_EVENT_TYPES);
    }

    @FXML
    private void ajouterEvenement() {
        try {
            // **Validation**
            if (!validateFields()) {
                return;
            }

            LocalDate eventDate = datePicker.getValue();
            ServiceEvenement serviceEvenement = new ServiceEvenement();

            Evenement evenement = new Evenement(
                    0,
                    txtNom.getText().trim(),
                    txtLieu.getText().trim(),
                    eventDate.toString(),
                    Integer.parseInt(txtCapacite.getText().trim()),
                    comboBoxType.getValue(),
                    txtOrganisateur.getText().trim(),
                    txtDescription.getText().trim(),
                    Double.parseDouble(txtPrix.getText().trim())
            );

            serviceEvenement.ajouterEvenement(evenement);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides pour la capacité et le prix.");
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur s'est produite. Veuillez vérifier vos entrées.");
        }
    }

    private boolean validateFields() {
        if (txtNom.getText().trim().isEmpty() ||
                txtDescription.getText().trim().isEmpty() ||
                txtLieu.getText().trim().isEmpty() ||
                txtCapacite.getText().trim().isEmpty() ||
                txtOrganisateur.getText().trim().isEmpty() ||
                txtPrix.getText().trim().isEmpty() ||
                datePicker.getValue() == null ||
                comboBoxType.getValue() == null) {

            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return false;
        }

        try {
            int capacite = Integer.parseInt(txtCapacite.getText().trim());
            if (capacite <= 0) {
                showAlert("Erreur", "La capacité doit être un nombre positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un nombre valide pour la capacité.");
            return false;
        }

        try {
            double prix = Double.parseDouble(txtPrix.getText().trim());
            if (prix < 0) {
                showAlert("Erreur", "Le prix ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un prix valide.");
            return false;
        }

        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date de l'événement ne peut pas être passée.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void closeWindow() {
        Platform.runLater(() -> {
            txtNom.getScene().getWindow().hide();

        });
    }

}
