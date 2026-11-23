package controller;

import entities.TypeUtilisateur;
import entities.Utilisateur;
import entities.vol;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.c_vol;
import utils.SessionManager;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjouterVolController {

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


    public void ajoutervol(ActionEvent actionEvent) {
        try {
            // Vérification des champs obligatoires
            if (isFieldEmpty(compagnieField) || isFieldEmpty(disponibleField) ||
                    isFieldEmpty(aeroDepartField) || isFieldEmpty(aeroArriveeField) ||
                    isFieldEmpty(numeroVolField) || isFieldEmpty(prixField) ||
                    dateDepartPicker.getValue() == null || dateArriveePicker.getValue() == null) {
                showAlert("Erreur", "Tous les champs doivent être remplis.", Alert.AlertType.ERROR);
                return;
            }

            // Vérification et récupération du numéro de vol (Format : ex. "AF1234")
            String numero_vol = numeroVolField.getText().trim();


            // Vérification et récupération du nombre de places disponibles
            if (!disponibleField.getText().matches("\\d+")) {
                showAlert("Erreur", "Le nombre de places disponibles doit être un entier positif.", Alert.AlertType.ERROR);
                return;
            }
            int disponible = Integer.parseInt(disponibleField.getText().trim());

            // Vérification du nom de la compagnie (uniquement lettres et espaces)
            String compagnie = compagnieField.getText().trim();
            if (!compagnie.matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s-]+$")) {
                showAlert("Erreur", "Le nom de la compagnie ne doit contenir que des lettres et des espaces.", Alert.AlertType.ERROR);
                return;
            }

            // Vérification des noms d’aéroports (alphanumériques, sans caractères spéciaux)
            String aero_depart = aeroDepartField.getText().trim();
            String aero_arrivee = aeroArriveeField.getText().trim();
            if (!aero_depart.matches("^[A-Za-z0-9\\s-]+$") || !aero_arrivee.matches("^[A-Za-z0-9\\s-]+$")) {
                showAlert("Erreur", "Les noms des aéroports doivent être alphanumériques et ne pas contenir de caractères spéciaux.", Alert.AlertType.ERROR);
                return;
            }

            // Vérification du prix (nombre décimal positif)
            if (!prixField.getText().matches("\\d+(\\.\\d{1,2})?")) {
                showAlert("Erreur", "Le prix doit être un nombre positif avec au maximum deux décimales.", Alert.AlertType.ERROR);
                return;
            }
            double prix = Double.parseDouble(prixField.getText().trim());

            // Vérification des dates
            LocalDate date_depart = dateDepartPicker.getValue();
            LocalDate date_arrivee = dateArriveePicker.getValue();

            if (date_depart.isBefore(LocalDate.now())) {
                showAlert("Erreur", "La date de départ ne peut pas être dans le passé.", Alert.AlertType.ERROR);
                return;
            }
            if (date_depart.isAfter(date_arrivee)) {
                showAlert("Erreur", "La date de départ ne peut pas être après la date d'arrivée.", Alert.AlertType.ERROR);
                return;
            }

            // Création de l'objet Vol
            vol vol = new vol(disponible, numero_vol, compagnie, aero_depart, aero_arrivee, prix,
                    Date.valueOf(date_depart), Date.valueOf(date_arrivee));

            // Debug: Vérifier l'objet avant insertion
            System.out.println("Ajout du vol : " + vol);

            // Ajout du vol à la base de données
            c_vol serviceVol = new c_vol();
            serviceVol.ajouter(vol);

            // Message de succès
            showAlert("Succès", "Vol ajouté avec succès.", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides pour les champs numériques.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de l'ajout du vol : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    public void navigatetovolutili(ActionEvent actionEvent) {
        try {
            FXMLLoader loader;



            if (isAdminUser()) {
                loader = new FXMLLoader(getClass().getResource("/Interfaceadmin.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/InterfaceClient.fxml"));
            }

            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) gotoafficher.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Méthode utilitaire pour vérifier si un champ est vide ou null
    private boolean isFieldEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    // Méthode utilitaire pour afficher une alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean isAdminUser() {
        Utilisateur utilisateur = SessionManager.getUtilisateur();
        return utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.ADMIN;
    }
}

