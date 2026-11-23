package tests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class TestLocationIQ {

    private static final String API_KEY = "pk.d23d607e10a7e14a3ec0d35a174b66e4"; // Remplacez par votre clé API LocationIQ

    public static void main(String[] args) {
        // Exemple de coordonnées de départ et d'arrivée
        double latDepart = 33.8439408;
        double lonDepart = 9.400138;
        double latArrivee = 36.800000;
        double lonArrivee = 10.170000;

        // URL de l'API LocationIQ pour obtenir les directions (coordonnées dans l'ordre longitude, latitude)
        String url = "https://us1.locationiq.com/v1/directions/driving/" + lonDepart + "," + latDepart + ";" + lonArrivee + "," + latArrivee +
                "?key=" + API_KEY +
                "&alternatives=true" +
                "&steps=true" +
                "&geometries=polyline" +
                "&overview=simplified" +
                "&annotations=true";

        // Création du client HTTP
        HttpClient client = HttpClient.newHttpClient();

        try {
            // Création de la requête GET
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Envoi de la requête et récupération de la réponse
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Vérification du code de statut de la réponse
            if (response.statusCode() == 200) {
                // La réponse est valide, on affiche le résultat
                JSONObject jsonResponse = new JSONObject(response.body());

                // Extraction de la distance et de la durée
                JSONObject route = jsonResponse.getJSONArray("routes").getJSONObject(0);  // Sélectionner la première route
                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);  // Sélectionner la première "leg"

                // Récupérer la distance et la durée
                double distance = leg.getDouble("distance") / 1000;  // Distance en kilomètres
                int duration = leg.getInt("duration");  // Durée en secondes

                // Convertir la durée en heures et minutes
                int hours = duration / 3600;
                int minutes = (duration % 3600) / 60;

                // Affichage des résultats
                System.out.println("Distance : " + distance + " km");
                System.out.println("Durée : " + hours + " heures et " + minutes + " minutes");
            } else {
                // Si le code de statut n'est pas 200, afficher une erreur
                System.out.println("Erreur API : Code de statut " + response.statusCode());
                System.out.println("Message d'erreur : " + response.body());
            }
        } catch (Exception e) {
            // Gestion des erreurs
            System.out.println("Erreur lors de la requête : " + e.getMessage());
            e.printStackTrace();  // Affiche la trace de l'exception pour un débogage plus facile
        }
    }
}
