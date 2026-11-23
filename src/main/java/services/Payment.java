package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.Evenement;
import entities.TypeUtilisateur;
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
import java.time.LocalDate;

public class Payment {

    private static final String PAYMEE_URL = "https://sandbox.paymee.tn/api/v2/payments/create";
    private static final String API_KEY = "9fc5a2c0405177043bc8be0a0e5abcd2e23f35d6";

    // Method to initiate payment based on User and Event details
    public static void initiatePayment(Utilisateur utilisateur, Evenement evenement, int nbPlaces) {
        try {
            // Calculate the total amount based on event price and number of places
            double totalAmount = evenement.getPrix() * nbPlaces;

            // Create JSON request body
            JsonObject paymentRequest = new JsonObject();
            paymentRequest.addProperty("amount", totalAmount); // Amount = event price * nb places
            paymentRequest.addProperty("note", "Payment for event: " + evenement.getNom()); // Event name in the note
            paymentRequest.addProperty("first_name", utilisateur.getNom()); // User's first name
            paymentRequest.addProperty("last_name", utilisateur.getPrenom()); // User's last name
            paymentRequest.addProperty("email", utilisateur.getEmail()); // User's email
            paymentRequest.addProperty("phone", utilisateur.getTelephone()); // User's phone
            paymentRequest.addProperty("return_url", "https://www.return_url.tn");
            paymentRequest.addProperty("cancel_url", "https://www.cancel_url.tn");
            paymentRequest.addProperty("webhook_url", "https://www.webhook_url.tn");
            paymentRequest.addProperty("order_id", String.valueOf(System.currentTimeMillis())); // Unique order ID

            // Send the request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMEE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Token " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(paymentRequest.toString()))
                    .build();

            // Handle the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the response JSON
            JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();

            if (responseJson.get("status").getAsBoolean()) {
                String paymentUrl = responseJson.getAsJsonObject("data").get("payment_url").getAsString();
                System.out.println("Payment URL: " + paymentUrl);

                // Open the payment page in JavaFX WebView
                openPaymentPage(paymentUrl);
            } else {
                System.out.println("Payment initiation failed: " + responseJson.get("message").getAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void openPaymentPage(String paymentUrl) {
        Platform.runLater(() -> {
            try {
                // Create a new WebView to display the payment page
                WebView webView = new WebView();
                webView.getEngine().load(paymentUrl);

                // Detect when the page changes (i.e., redirects to return_url)
                webView.getEngine().locationProperty().addListener((obs, oldUrl, newUrl) -> {
                    if (newUrl.startsWith("https://www.return_url.tn")) {
                        System.out.println("Payment Completed! Closing window...");
                        Stage stage = (Stage) webView.getScene().getWindow();
                        stage.close(); // Close the payment window
                    }
                });

                // Create a layout to hold the WebView
                StackPane root = new StackPane();
                root.getChildren().add(webView);

                // Create a new Scene and set it on a Stage (popup window)
                Stage paymentStage = new Stage();
                Scene scene = new Scene(root, 800, 600); // Adjust the size as needed
                paymentStage.setTitle("Payment Page");
                paymentStage.setScene(scene);
                paymentStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) {
        // Create an instance of Utilisateur (User)
        Utilisateur utilisateur = new Utilisateur(
                "John",
                "Doe",
                123456,
                "john.doe@example.com",
                "password",
                "123 Main St",
                "+21611222333",
                LocalDate.of(1990, 1, 1),
                TypeUtilisateur.VIP
        );

        // Create an instance of Evenement (Event)
        Evenement evenement = new Evenement(
                101,
                "Concert Event",
                "Carthage",
                "2025-03-01",
                500,
                "Music",
                "Music Org",
                "A great concert event",
                50.0
        );

        // Number of places booked by the user
        int nbPlaces = 2;

        // Initiate payment
        initiatePayment(utilisateur, evenement, nbPlaces);
    }


}
