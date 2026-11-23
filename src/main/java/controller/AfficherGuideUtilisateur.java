package controller;

import entities.Guide;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import services.GuideService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;


public class AfficherGuideUtilisateur {
    @FXML
    private ImageView guideImageView;
    @FXML
    private HBox guideNameHBox;
    @FXML
    private Label Telephone;
    @FXML
    private ListView<Guide> listView; // Utilisation du type générique
    @FXML
    private Label langueLabel;
    @FXML
    private VBox detailsVBox;
    @FXML
    private Label email;
    @FXML
    private Label nameLabel;

    private GuideService guideService;

    public AfficherGuideUtilisateur() throws SQLException {
        guideService = new GuideService();
    }

    @FXML
    public void initialize() {
        utils.ImageUtils.synchroniserDepuisSymfonyGuide();
        rafraichirListView();
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
                        Circle clip = new Circle(25, 25, 25);
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
                    detailsVBox.setVisible(true);
                } else {
                    detailsVBox.setVisible(false);
                }
            });

            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().selectFirst();
            } else {
                detailsVBox.setVisible(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateGuideDetails(Guide guide) {
        if (guide != null) {
            detailsVBox.setVisible(true);
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

        // Chemin local (JavaFX)
        File localFile = new File("src/main/resources/images/" + imageUrl);
        if (localFile.exists()) {
            return new Image(localFile.toURI().toString());
        }

        // Tentative de chargement via Symfony (uploads/guide)
        try {
            String remoteUrl = "http://127.0.0.1:8000/uploads/guide/" + imageUrl;
            return new Image(remoteUrl, true);
        } catch (Exception e) {
            System.out.println("❌ Impossible de charger l'image à distance : " + imageUrl);
        }

        // Fallback image par défaut
        return new Image(getClass().getClassLoader().getResource("images/default.jpg").toExternalForm());
    }

}
