package controller;

import entities.Hotel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.ServiceHotel;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class afficher_hotelController {

    @FXML
    private ListView<HBox> lv_hotels;
    @FXML
    private Button btnAjouterHotel;

    private final ServiceHotel serviceHotel = ServiceHotel.getInstance();

    @FXML
    public void initialize() {
        utils.ImageUtils.synchroniserDepuisSymfonyHotel(); // 🔁 synchronise depuis Symfony
        loadHotels();
    }

    /**
     * 🔹 Charger la liste des hôtels depuis la base de données
     */
    private void loadHotels() {
        lv_hotels.getItems().clear();
        try {
            List<Hotel> hotels = serviceHotel.afficherhotel();
            for (Hotel hotel : hotels) {
                lv_hotels.getItems().add(createHotelBox(hotel));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Impossible de charger les hôtels: " + e.getMessage());
        }
    }

    /**
     * 🔹 Créer une boîte (HBox) pour afficher les détails d'un hôtel
     */
    private HBox createHotelBox(Hotel hotel) {
        VBox hotelBox = new VBox(10);
        hotelBox.getStyleClass().add("hotel-box");

        // 📸 Chargement de l'image depuis resources/images/
        javafx.scene.image.ImageView hotelImage = new javafx.scene.image.ImageView();
        hotelImage.setFitWidth(150);
        hotelImage.setFitHeight(100);

        String imageName = hotel.getImage_hotel();
        java.io.File imgFile = new java.io.File("src/main/resources/images/" + imageName);
        javafx.scene.image.Image image;

        if (imgFile.exists()) {
            image = new javafx.scene.image.Image(imgFile.toURI().toString());
        } else {
            java.io.File def = new java.io.File("src/main/resources/images/image_defaut.png");
            if (def.exists()) {
                image = new javafx.scene.image.Image(def.toURI().toString());
            } else {
                image = new javafx.scene.image.Image("https://via.placeholder.com/150x100.png?text=Image+absente");
            }
        }

        hotelImage.setImage(image);

        Text hotelName = new Text("Nom : " + hotel.getNom());
        hotelName.getStyleClass().add("hotel-name");

        Text hotelPrice = new Text("Prix : " + hotel.getPrix_nuit() + " DT/nuit");
        hotelPrice.getStyleClass().add("hotel-price");

        Text hotelAddress = new Text("Adresse : " + hotel.getAdresse());
        hotelAddress.getStyleClass().add("hotel-address");

        hotelBox.getChildren().addAll(hotelImage, hotelName, hotelPrice, hotelAddress);

        HBox buttonsBox = new HBox(10);
        Button btnEdit = new Button("Modifier");
        btnEdit.setOnAction(e -> openEditHotel(hotel));
        btnEdit.getStyleClass().add("hotel-button");

        Button btnDelete = new Button("Supprimer");
        btnDelete.setOnAction(e -> supprimerHotel(hotel));
        btnDelete.getStyleClass().addAll("hotel-button", "hotel-delete-button");

        buttonsBox.getChildren().addAll(btnEdit, btnDelete);
        hotelBox.getChildren().add(buttonsBox);

        HBox finalBox = new HBox(hotelBox);
        finalBox.getStyleClass().add("hotel-box-container");

        return finalBox;
    }

    /**
     * 🔹 Ouvrir la fenêtre d'ajout d'un hôtel
     */
    @FXML
    private void openAddHotel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_hotel.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Hôtel");
            stage.show();
        } catch (IOException e) {
            //Robust error handling - this is crucial!
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la fenêtre d'ajout d'hôtel");
            alert.setContentText("Une erreur s'est produite lors du chargement du fichier FXML :\n" + e.getMessage());
            alert.showAndWait();
            e.printStackTrace(); // Print the stack trace to the console for debugging.
        }
    }

    @FXML
    private void RetournerPagePrecedente() {
        try {
            // Chargement de la page précédente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuAdmin.fxml")); // Remplacez par le chemin vers votre page précédente
            Parent root = loader.load();

            Stage stage = (Stage) btnAjouterHotel.getScene().getWindow(); // Utiliser le bouton d'ajout
            stage.setScene(new Scene(root));
            stage.show(); // Afficher la page précédente
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la page précédente.");
        }
    }
    /**
     * 🔹 Supprimer un hôtel donné après confirmation
     */
    private void supprimerHotel(Hotel hotel) {
        if (hotel != null && showConfirmationDialog("Supprimer Hôtel", "Êtes-vous sûr de vouloir supprimer cet hôtel ?")) {
            if (serviceHotel.supprimerHotelParNom(hotel.getNom())) {
                loadHotels(); // 🔄 Rafraîchir la liste après suppression
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Hôtel supprimé.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression.");
            }
        }
    }

    /**
     * 🔹 Ouvrir la fenêtre de modification d'un hôtel
     */
    private void openEditHotel(Hotel hotel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_hotel.fxml"));
            Parent root = loader.load();

            modifier_hotelController controller = loader.getController();
            controller.loadHotelInfo(hotel);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'hôtel");
            stage.showAndWait();

            loadHotels(); // 🔄 Rafraîchir après fermeture
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification.");
        }
    }

    /**
     * 🔹 Ouvrir une nouvelle fenêtre avec un titre
     */
    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);  // 🔹 La fenêtre s'ouvre en plein écran
            stage.showAndWait();  // ✅ Attendre la fermeture avant de rafraîchir
            loadHotels(); // 🔄 Rafraîchir la liste après fermeture de la fenêtre
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre.");
        }
    }

    /**
     * 🔹 Ouvrir une fenêtre avec un `Parent` déjà chargé et un titre personnalisé
     */
    private void openWindow(Parent root, String title) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.setMaximized(true);  // 🔹 Plein écran
        stage.showAndWait();  // ✅ Attendre fermeture pour rafraîchir
        loadHotels(); // 🔄 Rafraîchir la liste après fermeture
    }

    /**
     * 🔹 Afficher une boîte de confirmation avant suppression
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    /**
     * 🔹 Afficher une alerte générique
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
