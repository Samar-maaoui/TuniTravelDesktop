package controller;

import entities.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.PackVoyageService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModifierPackVoyageController {

    @FXML
    private TextField nomField, descriptionField, prixField, placesField;
    @FXML
    private ComboBox<String> comboVol; // Utiliser ComboBox de String
    @FXML
    private ComboBox<String> comboHotel; // Utiliser ComboBox de String
    @FXML
    private ComboBox<String> comboExcursion; // Utiliser ComboBox de String
    @FXML
    private Button btnModifier, btnAnnuler;

    private PackVoyageService packVoyageService;
    private PackVoyage packVoyage;

    public ModifierPackVoyageController() {
        packVoyageService = new PackVoyageService();
    }

    // Initialisation du formulaire avec les données du pack à modifier
    public void setPack(PackVoyage packVoyage) {
        this.packVoyage = packVoyage;
        nomField.setText(packVoyage.getNomPack());
        descriptionField.setText(packVoyage.getDetails());
        prixField.setText(String.valueOf(packVoyage.getPrixTotal()));
        placesField.setText(String.valueOf(packVoyage.getNombrePlacesRestants()));

        // Remplir les ComboBoxes avec les options disponibles (Vols, Hôtels, Excursions)
        try {
            List<vol> vols = packVoyageService.getAllVols();
            List<Hotel> hotels = packVoyageService.listerHotels();
            List<Excursion> excursions = packVoyageService.listerExcursions();

            // Formater les données et les ajouter dans les ComboBox
            comboVol.setItems(FXCollections.observableArrayList(formatVols(vols)));
            comboHotel.setItems(FXCollections.observableArrayList(formatHotels(hotels)));
            comboExcursion.setItems(FXCollections.observableArrayList(formatExcursions(excursions)));

            // Initialiser les ComboBox avec les valeurs actuelles du PackVoyage
            comboVol.setValue(formatVol(packVoyage.getVol()));
            comboHotel.setValue(formatHotel(packVoyage.getHotel()));
            comboExcursion.setValue(formatExcursion(packVoyage.getExcursion()));

        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    // Formater les objets vol en String pour les afficher dans le ComboBox
    private String formatVol(vol v) {
        if (v != null) {
            return v.getnumero_vol() + " - " + v.getCompagnie();
        }
        return "Aucun vol sélectionné";
    }

    // Formater les objets Hôtel en String pour les afficher dans le ComboBox
    private String formatHotel(Hotel h) {
        if (h != null) {
            return h.getNom() + " - " + h.getAdresse();
        }
        return "Aucun hôtel sélectionné";
    }

    // Formater les objets Excursion en String pour les afficher dans le ComboBox
    private String formatExcursion(Excursion e) {
        if (e != null) {
            return e.getNom() + " - " + e.getLieu();
        }
        return "Aucune excursion sélectionnée";
    }

    // Formater une liste de vols en Strings
    private List<String> formatVols(List<vol> vols) {
        List<String> formattedVols = new ArrayList<>();
        for (vol v : vols) {
            formattedVols.add(formatVol(v));
        }
        return formattedVols;
    }

    // Formater une liste d'hôtels en Strings
    private List<String> formatHotels(List<Hotel> hotels) {
        List<String> formattedHotels = new ArrayList<>();
        for (Hotel h : hotels) {
            formattedHotels.add(formatHotel(h));
        }
        return formattedHotels;
    }

    // Formater une liste d'excursions en Strings
    private List<String> formatExcursions(List<Excursion> excursions) {
        List<String> formattedExcursions = new ArrayList<>();
        for (Excursion e : excursions) {
            formattedExcursions.add(formatExcursion(e));
        }
        return formattedExcursions;
    }

    // Méthode pour annuler la modification et fermer le modal
    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    // Méthode pour modifier le pack de voyage
    @FXML
    private void handleModifier() {
        try {
            if (nomField.getText().isEmpty() || descriptionField.getText().isEmpty() || prixField.getText().isEmpty() || placesField.getText().isEmpty()) {
                afficherErreur("Tous les champs doivent être remplis.");
                return;
            }

            packVoyage.setNomPack(nomField.getText());
            packVoyage.setDetails(descriptionField.getText());
            packVoyage.setPrixTotal(Double.parseDouble(prixField.getText()));
            packVoyage.setNombrePlacesRestants(Integer.parseInt(placesField.getText()));

            // Récupérer les valeurs sélectionnées dans les ComboBoxes
            // Rechercher les objets correspondants à la sélection
            vol selectedVol = getVolFromCombo(comboVol.getValue());
            Hotel selectedHotel = getHotelFromCombo(comboHotel.getValue());
            Excursion selectedExcursion = getExcursionFromCombo(comboExcursion.getValue());

            packVoyage.setVol(selectedVol);
            packVoyage.setHotel(selectedHotel);
            packVoyage.setExcursion(selectedExcursion);

            packVoyageService.modifierPack(packVoyage);
            System.out.println("PackVoyage modifié avec succès !");
            closeWindow();

        } catch (SQLException e) {
            afficherErreur("Erreur lors de la modification du pack : " + e.getMessage());
        }
    }

    // Récupérer l'objet vol à partir de son affichage
    private vol getVolFromCombo(String selectedVolString) throws SQLException {
        for (vol v : packVoyageService.getAllVols()) {
            if (selectedVolString.equals(formatVol(v))) {
                return v;
            }
        }
        return null;
    }

    // Récupérer l'objet hôtel à partir de son affichage
    private Hotel getHotelFromCombo(String selectedHotelString) throws SQLException {
        for (Hotel h : packVoyageService.listerHotels()) {
            if (selectedHotelString.equals(formatHotel(h))) {
                return h;
            }
        }
        return null;
    }

    // Récupérer l'objet excursion à partir de son affichage
    private Excursion getExcursionFromCombo(String selectedExcursionString) throws SQLException {
        for (Excursion e : packVoyageService.listerExcursions()) {
            if (selectedExcursionString.equals(formatExcursion(e))) {
                return e;
            }
        }
        return null;
    }

    // Méthode pour afficher un message d'erreur
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Fermer la fenêtre/modal
    private void closeWindow() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }
}
