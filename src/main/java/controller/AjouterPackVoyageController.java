package controller;

import entities.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.PackVoyageService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.ImageUtils;
import utils.SessionManager;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AjouterPackVoyageController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField prixField;
    @FXML
    private TextField placesField;
    @FXML
    private ComboBox<vol> comboVol;  // ComboBox pour Vol
    @FXML
    private ComboBox<Hotel> comboHotel;  // ComboBox pour Hotel
    @FXML
    private ComboBox<Excursion> comboExcursion;  // ComboBox pour Excursion
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnRetour;
    @FXML
    private Button btnChoisirImage;
    @FXML
    private ImageView imageView;

    private PackVoyageService packVoyageService;

    public AjouterPackVoyageController() {
        packVoyageService = new PackVoyageService(); // Récupération du service Singleton
    }

    // Méthode pour ajouter un nouveau pack de voyage






    @FXML
    private void handleAjouter() throws SQLException {
        // Vérifier si l'utilisateur connecté est un ADMIN
        Utilisateur utilisateurConnecte = SessionManager.getInstance().getUtilisateur();
        if (utilisateurConnecte == null || utilisateurConnecte.getTypeUtilisateur() != TypeUtilisateur.ADMIN) {
            afficherErreur("Seul un administrateur peut ajouter un pack de voyage.");
            return;
        }

        // Validation des champs de saisie
        if (nomField.getText().isEmpty() || descriptionField.getText().isEmpty() || prixField.getText().isEmpty() || placesField.getText().isEmpty()) {
            afficherErreur("Tous les champs doivent être remplis.");
            return;
        }

        // Récupérer les éléments sélectionnés dans les ComboBox
        vol selectedVol = comboVol.getValue();
        Hotel selectedHotel = comboHotel.getValue();
        Excursion selectedExcursion = comboExcursion.getValue();

        // Créer un nouvel objet PackVoyage avec les informations récupérées
        PackVoyage pack = new PackVoyage();
        pack.setNomPack(nomField.getText());
        pack.setDetails(descriptionField.getText());
        pack.setPrixTotal(Double.parseDouble(prixField.getText()));
        pack.setNombrePlacesRestants(Integer.parseInt(placesField.getText()));

        // Assigner les éléments sélectionnés dans les ComboBox
        pack.setVol(selectedVol);  // Vol sélectionné
        pack.setHotel(selectedHotel);  // Hôtel sélectionné
        pack.setExcursion(selectedExcursion);  // Excursion sélectionnée

        // Si une image a été choisie, on la rattache au pack
        if (imageView.getImage() != null) {
            // Convertir l'image en URL pour la stocker dans la base
            String imagePath = imageView.getImage().getUrl();
            System.out.println("Chemin absolu de l'image choisie : " + imagePath);

            // Nettoyer le chemin de l'image en enlevant le préfixe 'file:' si présent
            if (imagePath.startsWith("file:/")) {
                imagePath = imagePath.substring(5); // Enlever 'file:/'
            }

            // Extraire seulement le nom de l'image (par exemple 'suisse.png')
            String imageName = new File(imagePath).getName();
            System.out.println("Nom de l'image à enregistrer : " + imageName);

            // Appeler la méthode enregistrerImage pour enregistrer l'image et obtenir le nom du fichier
            String finalImageName = ImageUtils.enregistrerImage(imagePath);
            System.out.println("Nom de l'image enregistrée dans le dossier : " + finalImageName);

            // Enregistrer seulement le nom du fichier image dans la base de données
            pack.setImageUrl(finalImageName);  // Utiliser le nom de l'image seulement
        } else {
            // Si aucune image n'est sélectionnée, utiliser une image par défaut
            System.out.println("Aucune image choisie, utilisation de l'image par défaut.");
            pack.setImageUrl("default.jpg");
        }

        // Ajouter le pack via le service
        try {
            packVoyageService.ajouterPack(pack);
            System.out.println("PackVoyage ajouté avec succès !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du pack : " + e.getMessage());
            afficherErreur("Erreur lors de l'ajout du pack.");
            return;
        }

        // Fermer la fenêtre après l'ajout
        closeWindow();
    }

    public void ajouterPack() throws SQLException {
        // Appel interne à la méthode privée handleAjouter
        handleAjouter();
    }

    // Choisir une image pour le pack


    @FXML
    private void handleChoisirImage() {
        // Ouvrir un sélecteur de fichiers pour choisir une image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            // Afficher l'image choisie dans l'ImageView
            imageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));

            // Appeler la méthode pour enregistrer l'image et obtenir le nom propre
            String imageName = ImageUtils.enregistrerImage(file.getAbsolutePath());

            // Enregistrer le nom de l'image dans la base de données ou dans l'objet PackVoyage
            // On stocke le nom de l'image dans le PackVoyage pour l'enregistrement dans la base de données
            PackVoyage pack = new PackVoyage();
            pack.setImageUrl(imageName);  // Utiliser ce nom pour l'enregistrer dans ta base de données
        }
    }



    // Retourner à l'interface précédente (Annuler l'ajout)
    @FXML
    private void handleRetour() {
        closeWindow();
    }

    // Afficher un message d'erreur
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Fermer la fenêtre/modal
    private void closeWindow() {
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.close();
    }

    // Initialisation des ComboBox avec les données disponibles
    @FXML
    public void initialize() {
        try {
            System.out.println("Début de la récupération des données...");

            // Récupérer les entités (vols, hôtels, excursions) en utilisant les méthodes du service
            List<vol> vols = packVoyageService.getAllVols(); // Vols
            List<Hotel> hotels = packVoyageService.listerHotels(); // Hôtels
            List<Excursion> excursions = packVoyageService.listerExcursions(); // Excursions

            // Ajouter les éléments dans les ComboBox respectifs
            comboVol.getItems().addAll(vols);        // Liste des vols
            comboHotel.getItems().addAll(hotels);    // Liste des hôtels
            comboExcursion.getItems().addAll(excursions); // Liste des excursions

            // Affichage des informations dans les ComboBox avec des détails plus significatifs

            // Vol ComboBox
            comboVol.setCellFactory(param -> new ListCell<vol>() {
                @Override
                protected void updateItem(vol item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getnumero_vol() + " - " + item.getCompagnie()); // Afficher le vol et la compagnie
                    }
                }
            });

            // Hotel ComboBox
            comboHotel.setCellFactory(param -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " | " + item.getAdresse()); // Afficher le nom et l'adresse de l'hôtel
                    }
                }
            });

            // Excursion ComboBox
            comboExcursion.setCellFactory(param -> new ListCell<Excursion>() {
                @Override
                protected void updateItem(Excursion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " | " + item.getLieu()); // Afficher le nom et le lieu de l'excursion
                    }
                }
            });

            // Display selected items (Optional - Can also be set on the ComboBox directly)
            comboVol.setButtonCell(new ListCell<vol>() {
                @Override
                protected void updateItem(vol item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getnumero_vol() + " - " + item.getCompagnie()); // Affichage du vol sélectionné
                    }
                }
            });

            comboHotel.setButtonCell(new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " | " + item.getAdresse()); // Affichage de l'hôtel sélectionné
                    }
                }
            });

            comboExcursion.setButtonCell(new ListCell<Excursion>() {
                @Override
                protected void updateItem(Excursion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " | " + item.getLieu()); // Affichage de l'excursion sélectionnée
                    }
                }
            });

            System.out.println("Données récupérées avec succès.");

        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des données : " + e.getMessage());
        }
    }

}
