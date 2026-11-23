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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VolApiController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    // On change le type pour utiliser des JSONObject
    @FXML
    private ListView<JSONObject> resultList;

    public void setSearchQuery(String query) {
        searchField.setText(query);
    }

    @FXML
    public void onSearchButtonClick() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            // Si aucun nom n'est entré, on peut vider la liste ou afficher un message personnalisé
            resultList.setItems(FXCollections.observableArrayList());
            return;
        }
        fetchAirportData(searchQuery);
    }

    private void fetchAirportData(String searchQuery) {
        try {
            String apiKey = "J/X+ra8R11vnuQmV0Oi3vA==ESn35lxveae0fsdz";
            if (apiKey == null || apiKey.isEmpty()) {
                resultList.setItems(FXCollections.observableArrayList());
                return;
            }

            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String apiUrl = "https://api.api-ninjas.com/v1/airports?name=" + encodedQuery;

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Api-Key", apiKey);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                resultList.setItems(FXCollections.observableArrayList());
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonResponse = new JSONArray(response.toString());
            ObservableList<JSONObject> airportList = FXCollections.observableArrayList();

            if (jsonResponse.length() == 0) {
                // Vous pouvez ajouter un message dans la ListView en cas d'absence de résultats, si besoin
            } else {
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject airport = jsonResponse.getJSONObject(i);
                    airportList.add(airport);
                }
            }

            resultList.setItems(airportList);
            resultList.setCellFactory(param -> new ListCell<JSONObject>() {
                @Override
                protected void updateItem(JSONObject airport, boolean empty) {
                    super.updateItem(airport, empty);
                    if (empty || airport == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setGraphic(createAirportCard(airport));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Vous pouvez afficher une erreur dans la ListView ou via une alerte
        }
    }

    // Méthode pour créer une carte d'affichage pour un aéroport
    private VBox createAirportCard(JSONObject airport) {
        // Les chemins vers les images doivent être présents dans votre dossier resources
        ImageView iconAirport = new ImageView(new Image(getClass().getResource("/images/plane2.png").toExternalForm()));
        iconAirport.setFitWidth(20);
        iconAirport.setFitHeight(20);

        ImageView iconCity = new ImageView(new Image(getClass().getResource("/images/ville.png").toExternalForm()));
        iconCity.setFitWidth(20);
        iconCity.setFitHeight(20);

        ImageView iconCountry = new ImageView(new Image(getClass().getResource("/images/earth.png").toExternalForm()));
        iconCountry.setFitWidth(20);
        iconCountry.setFitHeight(20);

        ImageView iconId = new ImageView(new Image(getClass().getResource("/images/iata.png").toExternalForm()));
        iconId.setFitWidth(20);
        iconId.setFitHeight(20);

        ImageView iconCoord = new ImageView(new Image(getClass().getResource("/images/regle.png").toExternalForm()));
        iconCoord.setFitWidth(20);
        iconCoord.setFitHeight(20);

        ImageView iconElevation = new ImageView(new Image(getClass().getResource("/images/regle.png").toExternalForm()));
        iconElevation.setFitWidth(20);
        iconElevation.setFitHeight(20);

        ImageView iconTimezone = new ImageView(new Image(getClass().getResource("/images/alarm.png").toExternalForm()));
        iconTimezone.setFitWidth(20);
        iconTimezone.setFitHeight(20);

        ImageView iconType = new ImageView(new Image(getClass().getResource("/images/buildingl.png").toExternalForm()));
        iconType.setFitWidth(20);
        iconType.setFitHeight(20);

        ImageView iconRunway = new ImageView(new Image(getClass().getResource("/images/piste.png").toExternalForm()));
        iconRunway.setFitWidth(20);
        iconRunway.setFitHeight(20);

        // Création des labels avec style
        Label nameLabel = new Label(" " + getJsonString(airport, "name", "Inconnu"));
        nameLabel.setStyle("-fx-font-size: 16px;");
        Label cityLabel = new Label(" Ville: " + getJsonString(airport, "city", "Inconnu"));
        cityLabel.setStyle("-fx-font-size: 16px;");
        Label countryLabel = new Label(" Pays: " + getJsonString(airport, "country", "Inconnu"));
        countryLabel.setStyle("-fx-font-size: 16px;");
        Label idLabel = new Label(" IATA: " + getJsonString(airport, "iata", "N/A")
                + " | ICAO: " + getJsonString(airport, "icao", "N/A"));
        idLabel.setStyle("-fx-font-size: 16px;");
        Label coordLabel = new Label(" Lat: " + airport.optDouble("latitude", 0)
                + " | Lon: " + airport.optDouble("longitude", 0));
        coordLabel.setStyle("-fx-font-size: 16px;");
        Label elevationLabel = new Label(" Élévation: " + airport.optInt("elevation", 0) + " m");
        elevationLabel.setStyle("-fx-font-size: 16px;");
        Label timezoneLabel = new Label(" Fuseau: " + getJsonString(airport, "timezone", "Non disponible"));
        timezoneLabel.setStyle("-fx-font-size: 16px;");
        Label typeLabel = new Label(" Type: " + getJsonString(airport, "type", "Non disponible"));
        typeLabel.setStyle("-fx-font-size: 16px;");
        Label runwayLabel = new Label(" Piste: " + getJsonString(airport, "runway_length", "Non spécifié")
                + ", Orientation: " + getJsonString(airport, "runway_orientation", "Non spécifié"));
        runwayLabel.setStyle("-fx-font-size: 16px;");

        // Organiser chaque attribut avec son icône dans un HBox
        HBox nameHBox = new HBox(5, iconAirport, nameLabel);
        nameHBox.setAlignment(Pos.CENTER_LEFT);
        HBox cityHBox = new HBox(5, iconCity, cityLabel);
        cityHBox.setAlignment(Pos.CENTER_LEFT);
        HBox countryHBox = new HBox(5, iconCountry, countryLabel);
        countryHBox.setAlignment(Pos.CENTER_LEFT);
        HBox idHBox = new HBox(5, iconId, idLabel);
        idHBox.setAlignment(Pos.CENTER_LEFT);
        HBox coordHBox = new HBox(5, iconCoord, coordLabel);
        coordHBox.setAlignment(Pos.CENTER_LEFT);
        HBox elevationHBox = new HBox(5, iconElevation, elevationLabel);
        elevationHBox.setAlignment(Pos.CENTER_LEFT);
        HBox timezoneHBox = new HBox(5, iconTimezone, timezoneLabel);
        timezoneHBox.setAlignment(Pos.CENTER_LEFT);
        HBox typeHBox = new HBox(5, iconType, typeLabel);
        typeHBox.setAlignment(Pos.CENTER_LEFT);
        HBox runwayHBox = new HBox(5, iconRunway, runwayLabel);
        runwayHBox.setAlignment(Pos.CENTER_LEFT);

        // Assemblage final de la carte
        VBox card = new VBox(5, nameHBox, cityHBox, countryHBox, idHBox, coordHBox,
                elevationHBox, timezoneHBox, typeHBox, runwayHBox);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; -fx-border-color: #ccc; -fx-padding: 10px;");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e9ecef; -fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; -fx-border-color: #bbb; -fx-padding: 10px;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; -fx-border-color: #ccc; -fx-padding: 10px;"));

        return card;
    }

    // Méthode utilitaire pour récupérer une chaîne depuis un JSONObject
    private String getJsonString(JSONObject json, String key, String defaultValue) {
        return json.has(key) && !json.isNull(key) ? json.optString(key, defaultValue) : defaultValue;
    }
}
