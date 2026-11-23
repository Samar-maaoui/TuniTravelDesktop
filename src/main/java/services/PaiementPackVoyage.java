package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.PackVoyage;
import entities.Utilisateur;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PaiementPackVoyage {

    private static final String PAYMEE_URL = "https://sandbox.paymee.tn/api/v2/payments/create";
    private static final String API_KEY = "9fc5a2c0405177043bc8be0a0e5abcd2e23f35d6";

    public static void initierPaiement(Utilisateur utilisateur, PackVoyage packVoyage, int nbPlaces) {
        try {
            double montantTotal = packVoyage.getPrixTotal() * nbPlaces;

            JsonObject requetePaiement = new JsonObject();
            requetePaiement.addProperty("amount", montantTotal);
            requetePaiement.addProperty("note", "Paiement pour le pack de voyage: " + packVoyage.getNomPack());
            requetePaiement.addProperty("first_name", utilisateur.getNom());
            requetePaiement.addProperty("last_name", utilisateur.getPrenom());
            requetePaiement.addProperty("email", utilisateur.getEmail());
            requetePaiement.addProperty("phone", utilisateur.getTelephone());
            requetePaiement.addProperty("return_url", "https://www.return_url.tn");
            requetePaiement.addProperty("cancel_url", "https://www.cancel_url.tn");
            requetePaiement.addProperty("webhook_url", "https://www.webhook_url.tn");
            requetePaiement.addProperty("order_id", String.valueOf(System.currentTimeMillis()));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMEE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Token " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requetePaiement.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();

            if (responseJson.get("status").getAsBoolean()) {
                String paymentUrl = responseJson.getAsJsonObject("data").get("payment_url").getAsString();
                System.out.println("URL de paiement : " + paymentUrl);

                // Ouvrir la page de paiement dans JavaFX WebView
                ouvrirPagePaiement(paymentUrl);
            } else {
                System.out.println("Échec de l'initiation du paiement : " + responseJson.get("message").getAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ouvrirPagePaiement(String paymentUrl) {
        Platform.runLater(() -> {
            try {
                WebView webView = new WebView();
                webView.getEngine().load(paymentUrl);

                StackPane root = new StackPane();
                root.getChildren().add(webView);

                Stage paymentStage = new Stage();
                Scene scene = new Scene(root, 800, 600);
                paymentStage.setTitle("Page de Paiement");
                paymentStage.setScene(scene);
                paymentStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
