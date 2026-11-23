package controller;

import entities.vol;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import services.c_vol;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalDate;

public class modifierVolController {

    @FXML
    private TextField compagnieField;
    @FXML
    private TextField disponibleField;
    @FXML
    private TextField aeroDepartField;
    @FXML
    private TextField aeroArriveeField;
    @FXML
    private DatePicker dateArriveePicker;
    @FXML
    private DatePicker dateDepartPicker;
    @FXML
    private TextField numeroVolField;
    @FXML
    private TextField prixField;
    @FXML
    private Button gotoafficher;
    private vol volActuel; // Stocke le vol actuel

    private c_vol serviceVol; // Instance du service pour accéder à la méthode modifier
    @FXML
    private Parent root;
    @FXML
    public void initialize() {
        Platform.runLater(() -> {

            // Réappliquer les styles CSS
            if (root.getScene() != null) {
                root.getScene().getStylesheets().clear();
                root.getScene().getStylesheets().add(getClass().getResource("/vol_styles.css").toExternalForm());
                System.out.println("Styles appliqués: " + root.getScene().getStylesheets());
            }
        });
    }
    // Constructeur du contrôleur (ou initialisation de l'objet serviceVol)
    public modifierVolController() {
        this.serviceVol = new c_vol(); // Initialiser le service
    }

    // Méthode pour initialiser les champs avec les données du vol sélectionné
    public void setVolActuel(vol vol) {
        this.volActuel = vol;

        // Initialiser les champs avec les valeurs du vol actuel
        numeroVolField.setText(vol.getnumero_vol());
        compagnieField.setText(vol.getCompagnie());
        aeroDepartField.setText(vol.getaero_depart());
        aeroArriveeField.setText(vol.getaero_arrivee());
        prixField.setText(String.valueOf(vol.getPrix()));
        disponibleField.setText(String.valueOf(vol.getDisponible()));

        // Formatage des dates pour les afficher dans les DatePicker
        if (vol.getDate_depart() != null) {
            LocalDate dateDepart = convertToLocalDate(vol.getDate_depart()); // Conversion en LocalDate
            dateDepartPicker.setValue(dateDepart); // Afficher dans le DatePicker
        }
        if (vol.getDate_arrivee() != null) {
            LocalDate dateArrivee = convertToLocalDate(vol.getDate_arrivee()); // Conversion en LocalDate
            dateArriveePicker.setValue(dateArrivee); // Afficher dans le DatePicker
        }
    }

    // Méthode utilitaire pour convertir une Date en LocalDate
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    // Méthode appelée lors de la soumission du formulaire de modification
    public void modifiervol(ActionEvent actionEvent) {
        // Récupérer les valeurs saisies par l'utilisateur
        String numeroVol = numeroVolField.getText();
        String compagnie = compagnieField.getText();
        String depart = aeroDepartField.getText();
        String arrivee = aeroArriveeField.getText();
        String prixText = prixField.getText();
        String disponibleText = disponibleField.getText();
        LocalDate dateDepartLocal = dateDepartPicker.getValue();
        LocalDate dateArriveeLocal = dateArriveePicker.getValue();

        // Validation des champs
        if (numeroVol.isEmpty() || compagnie.isEmpty() || depart.isEmpty() || arrivee.isEmpty() || prixText.isEmpty() || disponibleText.isEmpty() || dateDepartLocal == null || dateArriveeLocal == null) {
            showAlert(AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        double prix;
        int disponible;
        Date dateDepart, dateArrivee;

        try {
            prix = Double.parseDouble(prixText);
            disponible = Integer.parseInt(disponibleText);

            // Conversion des LocalDate en Date
            dateDepart = java.sql.Date.valueOf(dateDepartLocal);
            dateArrivee = java.sql.Date.valueOf(dateArriveeLocal);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Erreur de format", "Assurez-vous que les champs prix et disponible sont au bon format.");
            return;
        }

        // Création d'un nouvel objet vol avec les nouvelles données
        vol volUpdated = new vol();
        volUpdated.setnumero_vol(numeroVol);
        volUpdated.setCompagnie(compagnie);
        volUpdated.setAero_depart(depart);
        volUpdated.setAero_arriveea(arrivee);
        volUpdated.setPrix(prix);
        volUpdated.setDisponible(disponible);
        volUpdated.setDate_depart(dateDepart);
        volUpdated.setDate_arrivee(dateArrivee);

        // Appeler la méthode modifier du service c_vol pour mettre à jour le vol dans la base de données
        try {
            serviceVol.modifier(volUpdated);
            showAlert(AlertType.INFORMATION, "Succès", "Vol mis à jour avec succès !");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la mise à jour du vol.");
        }
    }
    public void gotoafficher(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherVol.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) gotoafficher.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Méthode utilitaire pour afficher les alertes
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}