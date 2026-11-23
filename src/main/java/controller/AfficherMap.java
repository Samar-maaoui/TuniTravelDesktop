package controller;

import entities.Excursion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Marker;
import services.ExcursionService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfficherMap {
    private static final String LOCATIONIQ_API_KEY = "pk.d23d607e10a7e14a3ec0d35a174b66e4";
    private final ExcursionService excursionService = new ExcursionService();

    @FXML
    private WebView webView;
    @FXML
    private TextArea textArea;

    private final List<String[]> lieux = new ArrayList<>();
    private final List<String[]> depart = new ArrayList<>();
    private Map<String, String> infoDistances = new HashMap<>();  // Stocker les distances et durées
    @FXML
    private ImageView imageView;
    @FXML
    private VBox mainContainer;


    public AfficherMap() throws SQLException {
    }

    @FXML
    public void initialize() throws SQLException {
        Platform.runLater(() -> {
            textArea.setVisible(true);
        });
        afficherCoordonnees();

    }
    private void calculerDistanceEtDuree() {
        HttpClient client = HttpClient.newHttpClient();



        for (int i = 0; i < depart.size(); i++) {
            String[] departPoint = depart.get(i);
            String[] lieuPoint = lieux.get(i);

            String latDepart = departPoint[0];
            String lonDepart = departPoint[1];
            String latLieu = lieuPoint[0];
            String lonLieu = lieuPoint[1];

            try {
                String url = "https://us1.locationiq.com/v1/directions/driving/" + lonDepart + "," + latDepart + ";" + lonLieu + "," + latLieu +
                        "?key=" + LOCATIONIQ_API_KEY +
                        "&alternatives=true" +
                        "&steps=true" +
                        "&geometries=polyline" +
                        "&overview=simplified" +
                        "&annotations=true";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray routes = jsonResponse.getJSONArray("routes");

                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        double distance = route.getDouble("distance") / 1000;  // en km
                        double duration = route.getDouble("duration") / 60;  // en minutes

                        String villeDepart = departPoint[2];
                        String lieuExcursion = lieuPoint[2];

                        String info = "De " + villeDepart + " à " + lieuExcursion + ":\n" +
                                "Distance: " + String.format("%.2f", distance) + " km\n" +
                                "Durée: " + String.format("%.2f", duration) + " min";

                        // Stocker dans la map
                        infoDistances.put(lieuExcursion, info);

                        System.out.println(info);
                    }
                } else {
                    System.out.println("Erreur API pour " + departPoint[2] + " -> " + lieuPoint[2]);
                }
            } catch (Exception e) {
                System.out.println("Erreur pour " + departPoint[2] + " -> " + lieuPoint[2] + ": " + e.getMessage());
            }
        }
    }

    private void afficherCoordonnees() throws SQLException {
        List<Excursion> excursions = excursionService.lire();
        if (excursions.isEmpty()) {
            textArea.setText("Aucune excursion trouvée.");
            return;
        }

        StringBuilder result = new StringBuilder();
        HttpClient client = HttpClient.newHttpClient();
        List<String[]> lieuxTemp = new ArrayList<>();

        for (Excursion excursion : excursions) {
            try {
                String encodedAdresse = URLEncoder.encode(excursion.getLieu(), StandardCharsets.UTF_8);
                String url = "https://us1.locationiq.com/v1/search.php?key=" + LOCATIONIQ_API_KEY +
                        "&q=" + encodedAdresse + "&format=json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONArray jsonArray = new JSONArray(response.body());
                    if (!jsonArray.isEmpty()) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String lat = jsonObject.getString("lat");
                        String lon = jsonObject.getString("lon");

                        // Ajouter toutes les infos nécessaires
                        lieuxTemp.add(new String[]{
                                lat, lon,
                                excursion.getNom(),
                                excursion.getVilleDepart(),
                                excursion.getLieu(),
                                String.valueOf(excursion.getNbPlace()),
                                excursion.getImage()

                        });

                       /* result.append(excursion.getNom()).append("   :  ")
                                .append(excursion.getVilleDepart()).append(" -> ")
                                .append(excursion.getLieu()).append(", Places restantes:  ")
                                .append(excursion.getNbPlace()).append("\n");*/
                    } else {
                        result.append("Aucune coordonnée trouvée pour ").append(excursion.getLieu()).append("\n");
                    }
                } else {
                    result.append("Erreur API pour ").append(excursion.getLieu())
                            .append(" (Code: ").append(response.statusCode()).append(")\n");
                }
            } catch (Exception e) {
                result.append("Erreur pour ").append(excursion.getLieu()).append(": ")
                        .append(e.getMessage()).append("\n");
            }
        }

        textArea.setText(result.toString());
        lieux.clear();
        lieux.addAll(lieuxTemp);
        afficherSurCarte();
    }






    private void afficherSurCarte() {
        WebEngine webEngine = webView.getEngine();
        try {
            Path tempFile = Files.createTempFile("osm_map", ".html");
            Files.write(tempFile, getOSMHTML(lieux).getBytes(), StandardOpenOption.WRITE);
            webEngine.load(tempFile.toUri().toString());

            webEngine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", this);
                }
            });

        } catch (IOException e) {
            System.err.println("Erreur lors de la création du fichier HTML : " + e.getMessage());
        }
    }


    private String getOSMHTML(List<String[]> lieux) {
        StringBuilder markersScript = new StringBuilder();
        for (String[] lieu : lieux) {
            String lat = lieu[0];
            String lon = lieu[1];
            String nomExcursion = lieu[2];
            String villeDepart = lieu[3];
            String destination = lieu[4];
            String nbPlaces = lieu[5];
            String imageUrl = lieu[6];  // Assurez-vous que l'image URL ou chemin est dans le tableau

            markersScript.append("var marker = L.marker([").append(lat).append(", ").append(lon).append("]).addTo(map);\n")
                    .append("marker.bindPopup('<b>").append(nomExcursion).append("</b><br>Départ: ")
                    .append(villeDepart).append("<br>Destination: ").append(destination)
                    .append("<br>Places restantes: ").append(nbPlaces).append("<br>")
                    .append("<img src=\"").append(imageUrl).append("\" width=\"150\" height=\"100\" />');\n")
                    .append("marker.on('click', function() {\n")
                    .append("    console.log('Marqueur cliqué: " + nomExcursion + "');\n")
                    .append("    if (window.javaConnector) {\n")
                    .append("        window.javaConnector.updateLocationTextArea('")
                    .append(nomExcursion).append("', '").append(villeDepart)
                    .append("', '").append(destination).append("', '")
                    .append(nbPlaces).append("', '").append(imageUrl).append("');\n")  // Ajouter l'URL de l'image ici
                    .append("    }\n")
                    .append("});\n");
        }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8' />
    <meta name='viewport' content='width=device-width, initial-scale=1.0' />
    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />
    <script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>
    <style> 
        #map { width: 100%; height: 100vh; }
        body { margin: 0; }
    </style>
</head>
<body>
    <div id='map'></div>
    <script>
        console.log("Carte chargée, ajout des marqueurs...");
        var map = L.map('map').setView([36.8065, 10.1815], 10);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 18}).addTo(map);
        
        """ + markersScript.toString() + """
    </script>
</body>
</html>
""";
    }

    public void updateLocationTextArea(String nomExcursion, String villeDepart, String destination, String nbPlaces, String imageUrl) {
        System.out.println("📍 Excursion sélectionnée : " + nomExcursion);

        // Appliquer un style à la textArea
        textArea.setStyle("-fx-font-family: 'Arial Rounded MT Bold';" + // Police élégante
                "-fx-font-size: 16px;" + // Taille de police
                "-fx-text-fill: #0d4060;" + // Couleur du texte (bleu-gris)
                "-fx-background-color: #ECF0F1;" + // Fond clair
                "-fx-border-radius: 15px;" + // Bord arrondi
                "-fx-padding: 15px;" + // Espacement interne
                "-fx-border-color: #3498DB;" + // Bordure bleue
                "-fx-border-width: 2px;" + // Épaisseur de la bordure
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        // Met à jour la zone de texte avec du style
        textArea.setText("🗺️Excursion: " + nomExcursion + "\n"
                + "📍 Départ: " + villeDepart + "\n"
                + "🎯 Destination: " + destination + "\n"
                + "👥 Places restantes: " + nbPlaces + "\n");

        // Charge et affiche l'image avec style
        Image image = chargerImage(imageUrl);
        imageView.setImage(image);
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 15px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        // Encadrer l'image et le texte ensemble
        VBox container = new VBox(10, textArea, imageView);
        container.setStyle("-fx-background-color: white; -fx-border-radius: 15px; -fx-padding: 15px; -fx-border-color: #BDC3C7; -fx-border-width: 2px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        // Ajouter le conteneur au parent
        mainContainer.getChildren().clear();
        mainContainer.getChildren().add(container);
    }


    private Image chargerImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "default.jpg";
        }

        String imagePath = "C:\\Users\\User\\Desktop\\PIFINAL\\PIDEV\\src\\main\\resources\\images\\Excursions\\"+imageUrl;
        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            return new Image(imageFile.toURI().toString());
        } else {
            System.out.println("❌ Erreur : Image " + imageUrl + " introuvable, chargement de default.jpg");
            return new Image(getClass().getClassLoader().getResource("images/default.jpg").toExternalForm());
        }
    }

}
