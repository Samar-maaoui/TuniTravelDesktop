package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class weatherController {

    private static final String API_KEY = "161c0e0d7579426cac4205130250103";

    @FXML
    public TextField cityField;

    @FXML
    public Label cityLabel;
    @FXML
    public Label tempLabel;
    @FXML
    public Label conditionLabel;

    @FXML
    public ImageView weatherIcon;

    @FXML
    public FlowPane forecastPane;

    @FXML
    public void fetchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer une ville.");
            return;
        }

        String urlCurrent = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + city + "&aqi=no";
        String urlForecast = "https://api.weatherapi.com/v1/forecast.json?key=" + API_KEY + "&q=" + city + "&days=7&aqi=no";

        // Récupération des données actuelles
        JSONObject jsonResponse = getJsonResponse(urlCurrent);
        if (jsonResponse == null) {
            showAlert("Erreur", "Impossible de récupérer les données météo.");
            return;
        }

        String cityName = jsonResponse.getJSONObject("location").getString("name");
        String condition = jsonResponse.getJSONObject("current").getJSONObject("condition").getString("text");
        double tempC = jsonResponse.getJSONObject("current").getDouble("temp_c");
        String iconUrl = "https:" + jsonResponse.getJSONObject("current").getJSONObject("condition").getString("icon");

        // Mettre à jour l'interface pour les données actuelles
        cityLabel.setText("Ville : " + cityName);
        tempLabel.setText(tempC + "°C");
        conditionLabel.setText(condition);
        weatherIcon.setImage(new Image(iconUrl));

        // Récupération des prévisions
        jsonResponse = getJsonResponse(urlForecast);
        if (jsonResponse == null) {
            return;
        }

        JSONArray forecastArray = jsonResponse.getJSONObject("forecast").getJSONArray("forecastday");
        forecastPane.getChildren().clear();

        for (int i = 0; i < forecastArray.length(); i++) {
            JSONObject day = forecastArray.getJSONObject(i);
            String date = day.getString("date");
            double maxTemp = day.getJSONObject("day").getDouble("maxtemp_c");
            double minTemp = day.getJSONObject("day").getDouble("mintemp_c");
            String dayCondition = day.getJSONObject("day").getJSONObject("condition").getString("text");
            String dayIconUrl = "https:" + day.getJSONObject("day").getJSONObject("condition").getString("icon");

            // Carte prévision météo
            VBox dayBox = new VBox();
            dayBox.setSpacing(5);
            dayBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-padding: 10; -fx-border-radius: 10; -fx-alignment: center; -fx-pref-width: 100;");

            Label dateLabel = new Label(date);
            dateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

            ImageView dayIcon = new ImageView(new Image(dayIconUrl));
            dayIcon.setFitWidth(50);
            dayIcon.setFitHeight(50);

            Label tempLabel = new Label(maxTemp + "°C / " + minTemp + "°C");
            tempLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            Label conditionLabel = new Label(dayCondition);
            conditionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            dayBox.getChildren().addAll(dateLabel, dayIcon, tempLabel, conditionLabel);
            forecastPane.getChildren().add(dayBox);
        }

    }

    private JSONObject getJsonResponse(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");

            Scanner scanner = new Scanner(connection.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            return new JSONObject(response);
        } catch (IOException e) {
            return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
