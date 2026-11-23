package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entities.vol;
import services.c_vol;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class afficherVolController {
    @FXML
    private Button btnAjouterVol;
    @FXML
    private Button btnsupprimerVol;
    @FXML
    private ListView<vol> listViewVols;
    @FXML
    private TextField searchField;
    @FXML
    private AnchorPane root; // ✅ Vérifier que root est bien lié au FXML

    private final c_vol serviceVol = new c_vol();

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


    @FXML
    public void afficherVols() {
        try {
            // Charger les vols depuis le service
            List<vol> vols = serviceVol.getAllvols();

            // Vérifier si la liste est vide ou nulle
            if (vols == null || vols.isEmpty()) {
                System.out.println("Aucun vol à afficher.");
            }

            // Effacer les éléments existants de la ListView
            listViewVols.getItems().clear();

            // Ajouter tous les vols à la ListView
            listViewVols.getItems().addAll(vols);

            // Définir la cellule personnalisée
            listViewVols.setCellFactory(param -> new ListCell<vol>() {
                @Override
                protected void updateItem(vol v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Créer la carte de vol
                        VBox card = createFlightCard(v);
                        setGraphic(card);
                    }
                }
            });

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible d'afficher les vols", e);
        }
    }

    @FXML
    public void supprimerVol(ActionEvent actionEvent) {
        vol selectedVol = listViewVols.getSelectionModel().getSelectedItem();
        if (selectedVol != null) {
            try {
                // Supprimer le vol du service
                serviceVol.supprimerVol(selectedVol);

                // Rafraîchir la ListView après la suppression
                afficherVols();  // Recharger la liste après suppression

                showInfo("Suppression réussie", "Le vol a été supprimé avec succès.");
            } catch (SQLException e) {
                showError("Erreur", "Une erreur s'est produite lors de la suppression du vol.", e);
            }
        } else {
            showWarning("Aucun vol sélectionné", "Veuillez sélectionner un vol à supprimer.");
        }
    }

    @FXML
    public void goToAjouterVols() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterVol.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouterVol.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void chercherVol(ActionEvent actionEvent) {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            afficherVols();  // Si le champ est vide, afficher tous les vols
            return;
        }

        try {
            List<vol> vols = serviceVol.getAllvols();
            List<vol> filteredVols = vols.stream()
                    .filter(v -> v.getnumero_vol().toLowerCase().contains(searchText) ||
                            v.getCompagnie().toLowerCase().contains(searchText) ||
                            v.getaero_depart().toLowerCase().contains(searchText) ||
                            v.getaero_arrivee().toLowerCase().contains(searchText))
                    .toList();

            listViewVols.getItems().setAll(filteredVols);  // Mettre à jour la liste affichée
        } catch (SQLException e) {
            showError("Erreur de recherche", "Impossible de chercher les vols", e);
        }
    }

    @FXML
    public void trierVols(ActionEvent actionEvent) {
        try {
            List<vol> vols = serviceVol.getAllvols();
            vols.sort((v1, v2) -> Integer.compare(v2.getDisponible(), v1.getDisponible()));
            listViewVols.getItems().setAll(vols);  // Mettre à jour l'affichage après tri
        } catch (SQLException e) {
            showError("Erreur de tri", "Impossible de trier les vols", e);
        }
    }


    private VBox createFlightCard(vol v) {
        // 📌 Icône d'avion
        ImageView iconAvion = new ImageView(new Image(getClass().getResource("/images/plane2.png").toExternalForm()));
        iconAvion.setFitWidth(20);
        iconAvion.setFitHeight(20);

        // 📍 Icône de la compagnie
        ImageView iconCompagnie = new ImageView(new Image(getClass().getResource("/images/compagnie.png").toExternalForm()));
        iconCompagnie.setFitWidth(20);
        iconCompagnie.setFitHeight(20);

        // 🛫 Icône de départ
        ImageView iconDepart = new ImageView(new Image(getClass().getResource("/images/depart.png").toExternalForm()));
        iconDepart.setFitWidth(20);
        iconDepart.setFitHeight(20);

        // 🛬 Icône d'arrivée
        ImageView iconArrivee = new ImageView(new Image(getClass().getResource("/images/arrivee.png").toExternalForm()));
        iconArrivee.setFitWidth(20);
        iconArrivee.setFitHeight(20);

        // 💰 Icône de prix
        ImageView iconPrix = new ImageView(new Image(getClass().getResource("/images/money.png").toExternalForm()));
        iconPrix.setFitWidth(20);
        iconPrix.setFitHeight(20);

        // ✅ Icône de disponibilité
        ImageView iconDisponible = new ImageView(new Image(getClass().getResource("/images/yes.png").toExternalForm()));
        iconDisponible.setFitWidth(20);
        iconDisponible.setFitHeight(20);

        // 📅 Icône de date de départ et arrivée
        ImageView iconDateDepart = new ImageView(new Image(getClass().getResource("/images/d_depart.png").toExternalForm()));
        iconDateDepart.setFitWidth(20);
        iconDateDepart.setFitHeight(20);

        ImageView iconDateArrivee = new ImageView(new Image(getClass().getResource("/images/d_arrivee.png").toExternalForm()));
        iconDateArrivee.setFitWidth(20);
        iconDateArrivee.setFitHeight(20);

        // 🎨 Infos du vol
        Label volLabel = new Label(String.format("Vol: %s", v.getnumero_vol()));
        volLabel.setStyle("-fx-font-size: 18px; ");

        Label compagnieLabel = new Label(String.format("Compagnie: %s", v.getCompagnie()));
        compagnieLabel.setStyle("-fx-font-size: 18px; ");

        Label departLabel = new Label(String.format("Départ: %s", v.getaero_depart()));
        departLabel.setStyle("-fx-font-size: 18px;");

        Label arriveeLabel = new Label(String.format("Arrivée: %s", v.getaero_arrivee()));
        arriveeLabel.setStyle("-fx-font-size: 18px;");

        Label prixLabel = new Label(String.format("Prix: %.2f€", v.getPrix()));
        prixLabel.setStyle("-fx-font-size: 18px;");

        Label dispoLabel = new Label(String.format("Disponible: %s", v.getDisponible()));
        dispoLabel.setStyle("-fx-font-size: 18px;");

        Label dateDepartLabel = new Label(String.format("Date_Départ: %s", v.getDate_depart()));
        dateDepartLabel.setStyle("-fx-font-size: 18px;");

        Label dateArriveeLabel = new Label(String.format("Date_Arrivée: %s", v.getDate_arrivee()));
        dateArriveeLabel.setStyle("-fx-font-size: 18px;");

        // Organiser chaque attribut avec son icône dans un HBox
        HBox volHBox = new HBox(5, iconAvion, volLabel);
        HBox compagnieHBox = new HBox(5, iconCompagnie, compagnieLabel);
        HBox departHBox = new HBox(5, iconDepart, departLabel);
        HBox arriveeHBox = new HBox(5, iconArrivee, arriveeLabel);
        HBox prixHBox = new HBox(5, iconPrix, prixLabel);
        HBox dispoHBox = new HBox(5, iconDisponible, dispoLabel);
        HBox dateDepartHBox = new HBox(5, iconDateDepart, dateDepartLabel);
        HBox dateArriveeHBox = new HBox(5, iconDateArrivee, dateArriveeLabel);

        // Ajuster l'alignement des HBox
        volHBox.setAlignment(Pos.CENTER_LEFT);
        compagnieHBox.setAlignment(Pos.CENTER_LEFT);
        departHBox.setAlignment(Pos.CENTER_LEFT);
        arriveeHBox.setAlignment(Pos.CENTER_LEFT);
        prixHBox.setAlignment(Pos.CENTER_LEFT);
        dispoHBox.setAlignment(Pos.CENTER_LEFT);
        dateDepartHBox.setAlignment(Pos.CENTER_LEFT);
        dateArriveeHBox.setAlignment(Pos.CENTER_LEFT);

        // Bouton Modifier
        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.getStyleClass().add("modifier-button");
        btnModifier.setOnAction(event -> goTomodifierVols(v));

        // Organiser toutes les HBox dans une VBox
        VBox card = new VBox(10, volHBox, compagnieHBox, departHBox, arriveeHBox, prixHBox, dispoHBox, dateDepartHBox, dateArriveeHBox, btnModifier);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #ccc;");

        // Effet au survol
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e9ecef; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #bbb;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #ccc;"));

        return card;
    }














    private void goTomodifierVols(vol v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierVol.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showError(String title, String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
