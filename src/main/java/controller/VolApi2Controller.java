package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VolApi2Controller {

    private static final String API_KEY = "3a43e761d745fd85b4ab7c4886cfe138";  // Votre clé API
    private static final String API_URL = "http://api.aviationstack.com/v1/flights";  // URL de l'API

    @FXML
    private TextField flightNumberField;

    // On utilise une ListView qui contient des JSONObject pour chaque vol
    @FXML
    private ListView<JSONObject> flightListView;

    public void setSearchQuery(String query) {
        flightNumberField.setText(query);
    }

    @FXML
    private void fetchFlightDetails() {
        String flightNumber = flightNumberField.getText().trim();

        if (flightNumber.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un numéro de vol.");
            return;
        }

        // Construction de l'URL avec le numéro de vol et la clé API
        String url = API_URL + "?flight_iata=" + flightNumber + "&access_key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(java.net.URI.create(url)).build();

        // Envoi de la requête de façon asynchrone
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    System.out.println("Réponse brute : " + response);
                    updateListView(response);
                })
                .exceptionally(e -> {
                    showAlert("Erreur", "Impossible de récupérer les données du vol.");
                    return null;
                });
    }

    private void updateListView(String response) {
        JSONObject json = new JSONObject(response);
        flightListView.getItems().clear();

        if (json.has("data") && json.getJSONArray("data").length() > 0) {
            JSONArray flightArray = json.getJSONArray("data");
            ObservableList<JSONObject> flightList = FXCollections.observableArrayList();
            for (int i = 0; i < flightArray.length(); i++) {
                flightList.add(flightArray.getJSONObject(i));
            }
            flightListView.setItems(flightList);
            flightListView.setCellFactory(param -> new ListCell<JSONObject>() {
                @Override
                protected void updateItem(JSONObject flightData, boolean empty) {
                    super.updateItem(flightData, empty);
                    if (empty || flightData == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setGraphic(createFlightCard(flightData));
                    }
                }
            });
        } else {
            showAlert("Erreur", "Aucun vol trouvé pour ce numéro.");
        }
    }

    // Méthode pour créer une carte d'affichage pour un vol
    private VBox createFlightCard(JSONObject flightData) {
        // Extraction des informations du vol
        String flightNumber = flightData.getJSONObject("flight").optString("iata", "N/A");
        String airline = flightData.getJSONObject("airline").optString("name", "N/A");
        String departure = flightData.getJSONObject("departure").optString("estimated", "N/A");
        String arrival = flightData.getJSONObject("arrival").optString("estimated", "N/A");
        String status = flightData.optString("flight_status", "N/A");

        // Création des icônes (assurez-vous que les images existent dans le dossier /images/)
        ImageView iconFlight = new ImageView(new Image(getClass().getResource("/images/plane2.png").toExternalForm()));
        iconFlight.setFitWidth(20);
        iconFlight.setFitHeight(20);

        ImageView iconAirline = new ImageView(new Image(getClass().getResource("/images/buildingl.png").toExternalForm()));
        iconAirline.setFitWidth(20);
        iconAirline.setFitHeight(20);

        ImageView iconDeparture = new ImageView(new Image(getClass().getResource("/images/depart.png").toExternalForm()));
        iconDeparture.setFitWidth(20);
        iconDeparture.setFitHeight(20);

        ImageView iconArrival = new ImageView(new Image(getClass().getResource("/images/arrivee.png").toExternalForm()));
        iconArrival.setFitWidth(20);
        iconArrival.setFitHeight(20);

        ImageView iconStatus = new ImageView(new Image(getClass().getResource("/images/piste.png").toExternalForm()));
        iconStatus.setFitWidth(20);
        iconStatus.setFitHeight(20);

        // Création des labels avec style
        Label flightNumberLabel = new Label(" Vol: " + flightNumber);
        flightNumberLabel.setStyle("-fx-font-size: 16px;");
        Label airlineLabel = new Label(" Compagnie: " + airline);
        airlineLabel.setStyle("-fx-font-size: 16px;");
        Label departureLabel = new Label(" Départ: " + departure);
        departureLabel.setStyle("-fx-font-size: 16px;");
        Label arrivalLabel = new Label(" Arrivée: " + arrival);
        arrivalLabel.setStyle("-fx-font-size: 16px;");
        Label statusLabel = new Label("ℹ Statut: " + status);
        statusLabel.setStyle("-fx-font-size: 16px;");

        // Organisation de chaque attribut avec son icône dans un HBox
        HBox flightHBox = new HBox(5, iconFlight, flightNumberLabel);
        flightHBox.setAlignment(Pos.CENTER_LEFT);
        HBox airlineHBox = new HBox(5, iconAirline, airlineLabel);
        airlineHBox.setAlignment(Pos.CENTER_LEFT);
        HBox departureHBox = new HBox(5, iconDeparture, departureLabel);
        departureHBox.setAlignment(Pos.CENTER_LEFT);
        HBox arrivalHBox = new HBox(5, iconArrival, arrivalLabel);
        arrivalHBox.setAlignment(Pos.CENTER_LEFT);
        HBox statusHBox = new HBox(5, iconStatus, statusLabel);
        statusHBox.setAlignment(Pos.CENTER_LEFT);

        // Assemblage final de la carte
        VBox card = new VBox(5, flightHBox, airlineHBox, departureHBox, arrivalHBox, statusHBox);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-background-radius: 10px; " +
                "-fx-border-color: #ccc; -fx-padding: 10px;");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e9ecef; -fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; -fx-border-color: #bbb; -fx-padding: 10px;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; -fx-border-color: #ccc; -fx-padding: 10px;"));

        return card;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
