package controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class radarController {

    private static final String API_URL = "https://flight-radar1.p.rapidapi.com/flights/list-in-boundary?bl_lat=13.607884&bl_lng=100.641975&tr_lat=13.771029&tr_lng=100.861566&limit=300";
    private static final String API_KEY = "0d87b105c7mshbec63317fb28731p147f63jsnb31e1bef2997";  // Utilise ta propre clé API

    @FXML
    private WebView mapView;

    private WebEngine webEngine;

    @FXML
    public void initialize() {
        webEngine = mapView.getEngine();
        webEngine.load(getClass().getResource("/radar.html").toExternalForm());  // Charge la carte HTML

        // Attend que la carte soit chargée avant de récupérer les vols
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                System.out.println("✅ Carte chargée ! Récupération des vols...");
                loadFlights();
            }
        });
    }

    @FXML
    private void loadFlights() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-rapidapi-host", "flight-radar1.p.rapidapi.com");
            connection.setRequestProperty("x-rapidapi-key", API_KEY);  // Utilise la clé API

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("❌ Erreur : HTTP " + responseCode);
                return;
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray aircraftArray = jsonResponse.getJSONArray("aircraft");

            JSObject js = (JSObject) webEngine.executeScript("window");

            for (int i = 0; i < aircraftArray.length(); i++) {
                JSONArray flight = aircraftArray.getJSONArray(i);
                double latitude = flight.getDouble(2);
                double longitude = flight.getDouble(3);
                String flightNumber = (flight.isNull(11) || !(flight.get(11) instanceof String)) ? "Unknown" : flight.getString(11);

                if (!flightNumber.equals("Unknown")) {  // Ajouter uniquement les vols avec un numéro valide
                    System.out.println("✈️ Ajout du vol : " + flightNumber);
                    js.call("addPlaneMarker", latitude, longitude, flightNumber);  // Appelle la fonction JavaScript
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
