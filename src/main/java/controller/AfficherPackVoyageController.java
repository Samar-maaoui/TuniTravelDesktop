package controller;

import entities.PackVoyage;
import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import services.PackVoyageService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherPackVoyageController {

    @FXML
    private ListView<PackVoyage> lv_packVoyage; // Liste des packs de voyage

    @FXML
    private Button btn_modifier;  // Bouton pour modifier un pack
    @FXML
    private Button btn_supprimer; // Bouton pour supprimer un pack
    @FXML
    private Button btn_ajouter;   // Bouton pour ajouter un pack
    @FXML
    private TextField tf_recherche; // Champ de recherche pour filtrer par nom de pack
    @FXML
    private ComboBox<String> cb_tri; // ComboBox pour le tri
    @FXML
    private Button btn_dashboard;
    @FXML
    private Button btn_reservation;
    @FXML
    private ListView<String> lv_detailsPack; // Nouvelle ListView pour afficher les détails du pack

    private PackVoyageService packVoyageService;

    public AfficherPackVoyageController() {
        packVoyageService = new PackVoyageService(); // Utilisation du service PackVoyageService
    }

    // Initialisation du ListView avec les packs de voyage
    @FXML
    public void initialize() {
        try {
            List<PackVoyage> packs = packVoyageService.listerPacks();  // Récupérer les packs
            ObservableList<PackVoyage> observablePacks = FXCollections.observableArrayList(packs);
            lv_packVoyage.setItems(observablePacks);  // Afficher dans la liste

            // ✅ Cellule personnalisée avec image Unsplash
            lv_packVoyage.setCellFactory(listView -> new ListCell<>() {
                @Override
                protected void updateItem(PackVoyage pack, boolean empty) {
                    super.updateItem(pack, empty);
                    if (empty || pack == null) {
                        setText(null);
                    } else {
                        setText("📦 " + pack.getNomPack());
                    }
                }
            });

            // ✅ Initialiser les options de tri
            cb_tri.setItems(FXCollections.observableArrayList("Prix", "Places restantes", "Nom"));
            cb_tri.getSelectionModel().selectFirst(); // Par défaut, trier par prix

            // ✅ Action de recherche
            tf_recherche.textProperty().addListener((observable, oldValue, newValue) -> {
                rechercherPacks(newValue);
            });

            // ✅ Action de tri
            cb_tri.valueProperty().addListener((observable, oldValue, newValue) -> {
                trierPacks(newValue);
            });

            // ✅ Sélection d’un pack
            lv_packVoyage.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    afficherDetailsPack(newValue);
                } else {
                    clearDetails();
                }
            });

        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des packs de voyage : " + e.getMessage());
        }
    }


    // Méthode pour rechercher un pack par son nom
    // Méthode pour rechercher un pack par son nom (améliorée avec les streams)
    private void rechercherPacks(String keyword) {
        try {
            List<PackVoyage> filteredPacks = packVoyageService.listerPacks();  // Liste des packs
            List<PackVoyage> result = filteredPacks.stream()
                    .filter(pack -> pack.getNomPack().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList()); // Filtrer et collecter les résultats

            ObservableList<PackVoyage> observablePacks = FXCollections.observableArrayList(result);
            lv_packVoyage.setItems(observablePacks);

        } catch (SQLException e) {
            afficherErreur("Erreur lors de la recherche des packs : " + e.getMessage());
        }
    }

    // Méthode pour trier les packs selon le critère choisi (améliorée avec les streams)
    private void trierPacks(String criteria) {
        try {
            List<PackVoyage> sortedPacks = packVoyageService.listerPacks(); // Liste des packs

            // Tri des packs selon le critère sélectionné
            Comparator<PackVoyage> comparator = null;
            switch (criteria) {
                case "Prix":
                    comparator = Comparator.comparingDouble(PackVoyage::getPrixTotal);
                    break;
                case "Places restantes":
                    comparator = Comparator.comparingInt(PackVoyage::getNombrePlacesRestants);
                    break;
                case "Nom":
                    comparator = Comparator.comparing(PackVoyage::getNomPack, String::compareToIgnoreCase);
                    break;
            }

            // Utilisation de stream pour trier les packs
            if (comparator != null) {
                sortedPacks = sortedPacks.stream()
                        .sorted(comparator)
                        .collect(Collectors.toList());  // Appliquer le tri et collecter les résultats
            }

            ObservableList<PackVoyage> observablePacks = FXCollections.observableArrayList(sortedPacks);
            lv_packVoyage.setItems(observablePacks);

        } catch (SQLException e) {
            afficherErreur("Erreur lors du tri des packs : " + e.getMessage());
        }
    }


    // Méthode pour afficher les détails du pack sélectionné dans la ListView
    private void afficherDetailsPack(PackVoyage pack) {
        ObservableList<String> details = FXCollections.observableArrayList();

        // Ajouter les informations du pack dans la ListView
        details.add("Nom du pack : " + pack.getNomPack());
        details.add("Prix : " + pack.getPrixTotal() + " €");
        details.add("Places disponibles : " + pack.getNombrePlacesRestants());

        // Détails sur le vol
        if (pack.getVol() != null) {
            details.add("✈ Vol : " + pack.getVol().getnumero_vol() + " | " + pack.getVol().getCompagnie());
        } else {
            details.add("✈ Aucun vol sélectionné.");
        }

        // Détails sur l'hôtel
        if (pack.getHotel() != null) {
            details.add("🏨 Hôtel : " + pack.getHotel().getNom() + " | " + pack.getHotel().getAdresse());
        } else {
            details.add("🏨 Aucun hôtel sélectionné.");
        }

        // Détails sur l'excursion
        if (pack.getExcursion() != null) {
            details.add("🗺 Excursion : " + pack.getExcursion().getNom() + " | " + pack.getExcursion().getLieu());
        } else {
            details.add("🗺 Aucune excursion sélectionnée.");
        }

        // Afficher les détails dans la ListView
        lv_detailsPack.setItems(details);
    }

    // Méthode pour vider les détails affichés
    private void clearDetails() {
        lv_detailsPack.getItems().clear();
    }

    // Méthode appelée lors du clic sur le bouton "Modifier"
    @FXML
    private void handleModifierPack() {
        try {
            // Vérifiez si un pack est sélectionné
            PackVoyage selectedPack = lv_packVoyage.getSelectionModel().getSelectedItem();
            if (selectedPack == null) {
                afficherErreur("Aucun pack sélectionné.");
                return;
            }

            // Créer une nouvelle fenêtre modale pour la modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPackVoyage.fxml"));
            AnchorPane root = loader.load();

            // Récupérer le contrôleur de la fenêtre modale et passer le pack sélectionné
            ModifierPackVoyageController controller = loader.getController();
            controller.setPack(selectedPack); // Passer le pack sélectionné à la fenêtre de modification

            // Créer une nouvelle scène pour la fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Modifier Pack de Voyage");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Modalité pour garder l'interface d'administration en arrière-plan
            stage.showAndWait(); // Afficher la fenêtre en modal

            // Rafraîchir la liste après modification
            lv_packVoyage.getItems().clear();
            lv_packVoyage.getItems().addAll(packVoyageService.listerPacks()); // Rafraîchir la liste des packs

        } catch (IOException e) {
            afficherErreur("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
        } catch (SQLException e) {
            afficherErreur("Erreur lors de la mise à jour des packs : " + e.getMessage());
        }
    }

    // Méthode appelée lors du clic sur le bouton "Supprimer"
    @FXML
    private void handleSupprimerPack() {
        PackVoyage selectedPack = lv_packVoyage.getSelectionModel().getSelectedItem();
        if (selectedPack != null) {
            try {
                packVoyageService.supprimerPack(selectedPack);  // Supprimer le pack sélectionné
                lv_packVoyage.getItems().remove(selectedPack);  // Retirer le pack de la liste affichée
                System.out.println("Pack supprimé : " + selectedPack.getNomPack());
            } catch (SQLException e) {
                afficherErreur("Erreur lors de la suppression du pack : " + e.getMessage());
            }
        } else {
            afficherErreur("Aucun pack sélectionné.");
        }
    }

    // Méthode appelée lors du clic sur le bouton "Ajouter un Pack"
    @FXML
    private void handleAjouterPack() {
        try {
            // Créer une nouvelle fenêtre modal pour l'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPackVoyage.fxml")); // Chemin vers votre fichier FXML
            AnchorPane root = loader.load();

            // Stage pour la fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Pack de Voyage");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Ouvre la fenêtre en modal
            stage.showAndWait(); // Affiche la fenêtre en modal et attend sa fermeture

            // Rafraîchir la liste des packs après l'ajout
            lv_packVoyage.getItems().clear();
            lv_packVoyage.getItems().addAll(packVoyageService.listerPacks()); // Rafraîchir la liste des packs
        } catch (IOException | SQLException e) {
            afficherErreur("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
        }
    }

    // Méthode pour afficher un message d'erreur
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleGestionReservations() {
        try {
            // Charger la nouvelle interface (FXML) pour la gestion des réservations
            Parent root = FXMLLoader.load(getClass().getResource("/ReservationTrello.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Gestion des Réservations");
            stage.setScene(new Scene(root));
            stage.setResizable(false); // ❌ Bloque le redimensionnement

            stage.show();

            // Fermer la fenêtre actuelle si nécessaire
            Stage currentStage = (Stage) btn_reservation.getScene().getWindow();
            // Ferme la fenêtre actuelle si vous souhaitez la fermer

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenDashboard() {
        try {
            Utilisateur utilisateur = utils.SessionManager.getInstance().getUtilisateur();
            if (utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.ADMIN) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_admin_pv.fxml"));
                Parent root = loader.load();

                Stage dashboardStage = new Stage();
                dashboardStage.setTitle("Tableau de bord Administrateur");
                Scene scene = new Scene(root);

                // Affichage en plein écran sans minimisation
                dashboardStage.setScene(scene);
                dashboardStage.setFullScreen(true);
                dashboardStage.setResizable(false);

                dashboardStage.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Accès refusé");
                alert.setHeaderText(null);
                alert.setContentText("Seul un administrateur peut accéder au tableau de bord.");
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
