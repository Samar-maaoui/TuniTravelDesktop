package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CountryAPI {

    public Map<String, Object> getCountryInfo(String countryName) {
        Map<String, Object> countryInfo = new HashMap<>();

        try {
            // Formater le nom du pays pour qu'il soit valide pour l'URL
            String urlString = "https://restcountries.com/v3.1/name/" + countryName.toLowerCase();
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Traitement de la réponse JSON
            countryInfo = parseCountryInfo(response.toString());

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à l'API : " + e.getMessage());
            e.printStackTrace();
        }
        return countryInfo;
    }

    private Map<String, Object> parseCountryInfo(String jsonResponse) {
        Gson gson = new Gson();
        Map<String, Object> countryInfo = new HashMap<>();

        // Utilisation de Gson pour parser le JSON
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);

        if (jsonArray.size() > 0) {
            JsonObject countryObject = jsonArray.get(0).getAsJsonObject();

            // Extraire la capitale
            if (countryObject.has("capital")) {
                JsonArray capitalArray = countryObject.getAsJsonArray("capital");
                if (capitalArray.size() > 0) {
                    countryInfo.put("capital", capitalArray.get(0).getAsString());
                }
            }

            // Extraire la population
            if (countryObject.has("population")) {
                int population = countryObject.get("population").getAsInt();
                countryInfo.put("population", population);
            }

            // Extraire les langues
            if (countryObject.has("languages")) {
                JsonObject languages = countryObject.getAsJsonObject("languages");
                StringBuilder languageNames = new StringBuilder();
                languages.entrySet().forEach(entry -> languageNames.append(entry.getValue().getAsString()).append(", "));

                // Retirer la virgule finale
                if (languageNames.length() > 0) {
                    languageNames.setLength(languageNames.length() - 2); // Supprimer la dernière virgule
                }
                countryInfo.put("languages", languageNames.toString());
            }

            // Extraire les monnaies
            if (countryObject.has("currencies")) {
                JsonObject currencies = countryObject.getAsJsonObject("currencies");
                StringBuilder currencyNames = new StringBuilder();
                currencies.entrySet().forEach(entry -> currencyNames.append(entry.getValue().getAsJsonObject().get("name").getAsString()).append(", "));

                // Retirer la virgule finale
                if (currencyNames.length() > 0) {
                    currencyNames.setLength(currencyNames.length() - 2); // Supprimer la dernière virgule
                }
                countryInfo.put("currencies", currencyNames.toString());
            }
        }

        return countryInfo;
    }
}