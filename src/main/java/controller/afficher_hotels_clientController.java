package controller;

import entities.Hotel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.CountryAPI;
import services.ServiceHotel;
import services.ServiceReservationHotel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class afficher_hotels_clientController {

    @FXML private ListView<HBox> lv_hotels_client;
    @FXML private Button btnRetour, btnReserver, btnMesReservations, btnSearch;
    @FXML private VBox detailsContainer;
    @FXML private Label lblNom, lblPrix, lblAdresse, lblDescription, lblTel, lblEmail;
    @FXML private ImageView imgHotel;
    @FXML private TextField txtSearchHotel;
    @FXML private Label lblLanguages, lblCurrencies, lblPopulation, lblCapitale;

    private final ServiceHotel serviceHotel = ServiceHotel.getInstance();
    private final ServiceReservationHotel serviceReservationHotel = new ServiceReservationHotel();
    private Hotel hotelSelectionne;

    @FXML
    public void initialize() {
        utils.ImageUtils.synchroniserDepuisSymfonyHotel(); // 🔁 synchronise depuis Symfony

        loadHotelsDisponibles();
        detailsContainer.setVisible(false);
    }

    @FXML
    public void searchHotel() {
        loadFilteredHotels(txtSearchHotel.getText().trim().toLowerCase());
    }

    private void loadFilteredHotels(String searchTerm) {
        lv_hotels_client.getItems().clear();
        try {
            List<Hotel> hotels = serviceHotel.searchHotelsByName(searchTerm);
            for (Hotel hotel : hotels) {
                if (hotel.isEst_disponible()) {
                    lv_hotels_client.getItems().add(createHotelBox(hotel));
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Impossible de charger les hôtels : " + e.getMessage());
        }
    }

    private void loadHotelsDisponibles() {
        lv_hotels_client.getItems().clear();
        lv_hotels_client.refresh();
        try {
            List<Hotel> hotels = serviceHotel.afficherhotel();
            for (Hotel hotel : hotels) {
                if (hotel.isEst_disponible()) {
                    lv_hotels_client.getItems().add(createHotelBox(hotel));
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Impossible de charger les hôtels : " + e.getMessage());
        }
    }
    private HBox createHotelBox(Hotel hotel) {
        HBox hotelBox = new HBox(20);
        hotelBox.getStyleClass().add("hotel-box");

        ImageView hotelImage = new ImageView();
        hotelImage.setFitWidth(150);
        hotelImage.setFitHeight(100);

        String imageName = hotel.getImage_hotel();
        File imgFile = new File("src/main/resources/images/" + imageName);
        Image image;

        if (imgFile.exists()) {
            image = new Image(imgFile.toURI().toString());
        } else {
            System.err.println("❌ Image introuvable : " + imageName);
            File def = new File("src/main/resources/images/image_defaut.png");
            if (def.exists()) {
                image = new Image(def.toURI().toString());
            } else {
                System.err.println("❌ Image par défaut introuvable !");
                image = new Image("https://via.placeholder.com/150x100.png?text=Image+absente");
            }
        }

        hotelImage.setImage(image);

        VBox hotelInfo = new VBox(5);
        hotelInfo.getChildren().addAll(
                new Label(hotel.getNom()),
                new Label("Prix : " + hotel.getPrix_nuit() + " DT/nuit"),
                new Label("Localisation : " + hotel.getAdresse())
        );

        Button btnDetails = new Button("Voir détails");
        btnDetails.setOnAction(e -> afficherDetailsHotel(hotel));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hotelBox.getChildren().addAll(hotelImage, hotelInfo, spacer, btnDetails);
        hotelBox.setUserData(hotel);
        return hotelBox;
    }



    private void afficherDetailsHotel(Hotel hotel) {
        hotelSelectionne = hotel;

        lblNom.setText(hotel.getNom());
        lblPrix.setText("Prix/Nuit : " + hotel.getPrix_nuit() + " DT");
        lblAdresse.setText("Adresse : " + hotel.getAdresse());
        lblDescription.setText("Description : " + hotel.getDescription());
        lblTel.setText("Téléphone : " + hotel.getTel());
        lblEmail.setText("Email Contact : " + hotel.getEmail_contact());

        if (imgHotel != null) {
            String imageName = hotel.getImage_hotel();
            File imgFile = new File("src/main/resources/images/" + imageName);
            if (imgFile.exists()) {
                imgHotel.setImage(new Image(imgFile.toURI().toString()));
            } else {
                File def = new File("src/main/resources/images/image_defaut.png");
                if (def.exists()) {
                    imgHotel.setImage(new Image(def.toURI().toString()));
                } else {
                    imgHotel.setImage(new Image("https://via.placeholder.com/300x200.png?text=Image+manquante"));
                }
            }
        }

        String[] parts = hotel.getAdresse().split(",");
        String countryName = parts.length > 1 ? parts[1].trim() : parts[0].trim();

        Map<String, Object> countryInfo = new CountryAPI().getCountryInfo(countryName);
        if (countryInfo != null) {
            lblCapitale.setText((String) countryInfo.getOrDefault("capital", "Non disponible"));
            lblPopulation.setText(String.valueOf(countryInfo.getOrDefault("population", "Non disponible")));
            lblLanguages.setText((String) countryInfo.getOrDefault("languages", "Non disponible"));
            lblCurrencies.setText((String) countryInfo.getOrDefault("currencies", "Non disponible"));
        } else {
            lblCapitale.setText("Non disponible");
            lblPopulation.setText("Non disponible");
            lblLanguages.setText("Non disponible");
            lblCurrencies.setText("Non disponible");
        }
        detailsContainer.setVisible(true);
    }


    @FXML
    private void allerReservation() {
        if (hotelSelectionne == null) {
            showAlert("Aucun hôtel sélectionné", "Veuillez sélectionner un hôtel avant de réserver.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_reservationhotel.fxml"));
            Parent root = loader.load();
            ajouter_reservationhotelController controller = loader.getController();
            controller.setId_hotel(hotelSelectionne.getId_hotel(), hotelSelectionne.getPrix_nuit());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une réservation - " + hotelSelectionne.getNom());
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de réservation.");
        }
    }

    @FXML
    private void retourMenu() {
        detailsContainer.setVisible(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void allerMesReservations() {
        if (hotelSelectionne == null) {
            showAlert("Aucun hôtel sélectionné", "Veuillez sélectionner un hôtel pour voir ses réservations.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Afficher_ReservationsController.fxml"));
            Parent root = loader.load();
            Afficher_ReservationsController controller = loader.getController();
            controller.setId_hotel(hotelSelectionne.getId_hotel());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Réservations - " + hotelSelectionne.getNom());
            stage.show();
        } catch (IOException | SQLException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre des réservations.");
            e.printStackTrace();
        }
    }
}