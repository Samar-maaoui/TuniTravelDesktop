package controller;

import entities.Guide;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import services.GuideService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AffGuide {

    @FXML
    private ListView<Guide> listView;
    @FXML
    private ImageView guideImageView;
    @FXML
    private Label nameLabel, langueLabel, Telephone, email;
    @FXML
    private VBox detailsVBox;
    @FXML
    private Button ajouter;
    @FXML
    private HBox guideNameHBox;

    private final GuideService guideService;
    @FXML
    private Button btnModidfier;
    @FXML
    private Button btnSupprimer;

    public AffGuide() throws SQLException {
        guideService = new GuideService();
    }

    @FXML
    public void initialize() {
        utils.ImageUtils.synchroniserDepuisSymfonyGuide(); // 🔁 synchroniser avant tout

        rafraichirListView();
    }

    @FXML
    public void handleAjouterGuide() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterGuide.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Guide");
            stage.showAndWait();  // Attend la fermeture de la fenêtre avant de continueraaaaaaaa

            rafraichirListView();  // 🔄 Rafraîchir la liste après l'ajout
            listView.refresh();    // Forcer le rafraîchissement
            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().selectLast();  // Sélectionne le dernier guide ajouté
                updateGuideDetails(listView.getSelectionModel().getSelectedItem());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void handleModifier(ActionEvent event) {
        Guide selectedGuide = listView.getSelectionModel().getSelectedItem();
        if (selectedGuide != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireModification.fxml"));
                Parent root = loader.load();

                // Récupérer le contrôleur de modification
                FormulaireModificationController controller = loader.getController();
                controller.setGuide(selectedGuide); // Envoyer le guide à modifier

                // Afficher la fenêtre de modification
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modifier Guide");
                stage.showAndWait();

                // Rafraîchir la liste après modification
                rafraichirListView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void rafraichirListView() {
        try {
            listView.getItems().clear();
            List<Guide> guides = guideService.lire();
            listView.getItems().addAll(guides);

            listView.setCellFactory(param -> new ListCell<Guide>() {
                @Override
                protected void updateItem(Guide item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        ImageView imageView = new ImageView(chargerImage(item.getImage()));
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        Circle clip = new Circle(25, 25, 25);  // Centre (25, 25) avec un rayon de 25 (la moitié de la taille de l'image)
                        imageView.setClip(clip);

                        Label nameLabel = new Label(item.getNom() + " " + item.getPrenom());

                        VBox vBox = new VBox(nameLabel);
                        vBox.setSpacing(5);

                        HBox hBox = new HBox(imageView, vBox);
                        hBox.setSpacing(10);

                        setGraphic(hBox);
                    }
                }
            });

            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateGuideDetails(newValue);
                    detailsVBox.setVisible(true); // ✅ S'assurer que la boîte de détails est bien affichée
                } else {
                    detailsVBox.setVisible(false); // ✅ Cacher les détails si aucun guide n'est sélectionné
                }
            });

            // ✅ Sélection automatique du premier élément si la liste n'est pas vide
            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().selectFirst();
            } else {
                detailsVBox.setVisible(false); // ✅ Si la liste est vide, cacher la boîte de détails
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateGuideDetails(Guide guide) {
        if (guide != null) {
            detailsVBox.setVisible(true); // S'assurer que la boîte de détails est affichée
            guideImageView.setImage(chargerImage(guide.getImage()));
            nameLabel.setText(guide.getNom() + " " + guide.getPrenom());
            langueLabel.setText(guide.getLangue() != null ? guide.getLangue() : "N/A");
            email.setText(guide.getEmail() != null ? guide.getEmail() : "N/A");
            Telephone.setText(guide.getTelephone() != null ? guide.getTelephone() : "N/A");
        }
    }

    private Image chargerImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "default.png";
        }

        // ✅ Corriger ici le chemin réel du fichier
        File imageFile = new File("src/main/resources/images/" + imageUrl);

        if (imageFile.exists()) {
            return new Image(imageFile.toURI().toString());
        } else {
            System.out.println("❌ Erreur : Image " + imageUrl + " introuvable, chargement de default.jpg");
            return new Image(getClass().getClassLoader().getResource("images/default.jpg").toExternalForm());
        }
    }


    public void handleSupprimer(ActionEvent actionEvent) {
        Guide selectedGuide = listView.getSelectionModel().getSelectedItem();
        if (selectedGuide != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le guide ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer " + selectedGuide.getNom() + " " + selectedGuide.getPrenom() + " ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        guideService.supprimer(selectedGuide.getIdGuide()); // Suppression de la BD

                        // 🔄 Rafraîchir la liste après suppression
                        rafraichirListView();


                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible de supprimer le guide.");
                    }
                }
            });
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un guide à supprimer.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
