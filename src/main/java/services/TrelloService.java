package services;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import entities.ReservationPackVoyage;
import entities.StatutReservation;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrelloService {
    private static final String API_KEY = "4f59d3c3fdc8a7843f9271b8b1dfdb95";
    private static final String API_TOKEN = "ATTA563e7b3a64feaa0b24244e4e3101d79eb3922670ac70e903ab36787df48a1b7911D3E566";

    // IDs des listes Trello par statut
    private static final String LIST_ID_EN_ATTENTE = "67c731de6d14cc4e4e30c381";
    private static final String LIST_ID_CONFIRMEES = "67c731eb2358efd035b7c03f";
    private static final String LIST_ID_ANNULEES = "67c731f947e7d389ab8aaa85";

    /**
     * Récupère l'ID de la liste Trello en fonction du statut.
     */
    private static String getTrelloListId(StatutReservation statut) {
        return switch (statut) {
            case EN_ATTENTE -> LIST_ID_EN_ATTENTE;
            case CONFIRMEE -> LIST_ID_CONFIRMEES;
            case ANNULEE -> LIST_ID_ANNULEES;
        };
    }
    public static String getListIdByStatut(StatutReservation statut) {
        return getTrelloListId(statut);
    }



    /**
     * Synchronise les réservations avec Trello.
     */
    public static void synchroniserReservationsAvecTrello() {
        ReservationPackVoyageService reservationService = new ReservationPackVoyageService();

        try {
            int utilisateurId = SessionManager.getInstance().getUtilisateur().getIdUtilisateur();
            List<ReservationPackVoyage> reservations = new ReservationPackVoyageService().listerReservations(utilisateurId);

            for (ReservationPackVoyage reservation : reservations) {
                ajouterOuMettreAJourCarteTrello(reservation);
            }
        } catch (SQLException | UnirestException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ajoute ou met à jour une carte Trello pour une réservation.
     */
    private static void ajouterOuMettreAJourCarteTrello(ReservationPackVoyage reservation) throws UnirestException {
        String listId = getTrelloListId(reservation.getStatut());

        // Vérifier si une carte Trello existe déjà pour cette réservation
        String cardId = trouverCarteTrello(reservation.getIdReservation());

        if (cardId == null) {
            // ✅ Créer une nouvelle carte SEULEMENT si elle n'existe pas déjà
            createTrelloCard(reservation);
        } else {
            // ✅ Déplacer la carte dans la bonne liste si son statut a changé
            moveTrelloCard(cardId, reservation.getStatut());
        }
    }




    /**
     * Vérifie si une carte Trello existe déjà pour cette réservation.
     */
    private static String trouverCarteTrello(int reservationId) throws UnirestException {
        for (StatutReservation statut : StatutReservation.values()) {
            HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/lists/" + getTrelloListId(statut) + "/cards")
                    .queryString("key", API_KEY)
                    .queryString("token", API_TOKEN)
                    .header("Accept", "application/json")
                    .asJson();

            for (Object obj : response.getBody().getArray()) {
                if (obj instanceof org.json.JSONObject card) {
                    // Vérifier si l'ID de la réservation est contenu dans la description (car masqué du titre)
                    if (card.has("desc") && card.getString("desc").contains("ID Réservation: " + reservationId)) {
                        return card.getString("id"); // Retourne l'ID de la carte trouvée
                    }
                }
            }
        }
        return null; // Aucun résultat trouvé
    }



    /**
     * Crée une carte Trello pour une réservation.
     */
    public static String createTrelloCard(ReservationPackVoyage reservation) throws UnirestException {
        String listId = getTrelloListId(reservation.getStatut());

        // ✅ Nom de la carte (Pack + Email client)
        String cardName = "🛫 " + reservation.getPackVoyage().getNomPack() + " | 📧 " + reservation.getUtilisateur().getEmail();

        // 🕵️ **Utilisation de caractères invisibles pour cacher l'ID**
        String hiddenSpace = "\n\n\n\n\n\n\n\n\n\n\n\u200B\u200B\u200B\u200B\u200B\u200B\u200B\u200B\u200B"; // Espaces invisibles
        String hiddenId = "ID Réservation: " + reservation.getIdReservation(); // Ajout de l'ID à la fin

        // ✅ Description sans affichage apparent de l'ID
        String cardDescription =
                "👤 Client: " + reservation.getUtilisateur().getNom() + "\n" +
                        "📧 Email: " + reservation.getUtilisateur().getEmail() + "\n" +
                        "👥 Places: " + reservation.getPlacesDemandees() + "\n" +
                        "💰 Total: " + reservation.getMontantTotal() + "€\n" +
                        "📅 Date: " + reservation.getDateReservation() + "\n" +
                        "💳 Paiement: " + reservation.getStatutPaiement() + hiddenSpace + hiddenId; // ✅ ID masqué loin dans la description

        // ✅ Création de la carte
        HttpResponse<JsonNode> response = Unirest.post("https://api.trello.com/1/cards")
                .queryString("idList", listId)
                .queryString("key", API_KEY)
                .queryString("token", API_TOKEN)
                .queryString("name", cardName)
                .queryString("desc", cardDescription)
                .header("Accept", "application/json")
                .asJson();

        if (response.getStatus() == 200) {
            return response.getBody().getObject().getString("id");
        } else {
            System.err.println("❌ Erreur lors de la création de la carte Trello.");
            return null;
        }
    }




    /**
     * Déplace une carte Trello vers une autre liste selon le statut.
     */
    public static boolean moveTrelloCard(String cardId, StatutReservation newStatut) throws UnirestException {
        String newListId = getTrelloListId(newStatut);

        HttpResponse<JsonNode> response = Unirest.put("https://api.trello.com/1/cards/" + cardId)
                .queryString("key", API_KEY)
                .queryString("token", API_TOKEN)
                .queryString("idList", newListId)
                .header("Accept", "application/json")
                .asJson();

        return response.getStatus() == 200;
    }

    /**
     * Supprime une carte Trello.
     */
    /**
     * Supprime une carte Trello.
     */
    /**
     * Supprime une carte Trello.
     */
    public static boolean deleteTrelloCard(String cardId) throws UnirestException {
        if (cardId == null || cardId.isEmpty()) {
            System.err.println("❌ Erreur : L'ID de la carte Trello est invalide !");
            return false;
        }

        System.out.println("🗑️ Tentative de suppression de la carte Trello avec ID : " + cardId);

        // 🔎 Vérifier si la carte existe avant de la supprimer
        HttpResponse<JsonNode> checkResponse = Unirest.get("https://api.trello.com/1/cards/" + cardId)
                .queryString("key", API_KEY)
                .queryString("token", API_TOKEN)
                .header("Accept", "application/json")
                .asJson();

        if (checkResponse.getStatus() != 200) {
            System.err.println("❌ La carte avec ID " + cardId + " n'existe pas sur Trello.");
            return false;
        }

        // 🗑️ Suppression de la carte
        HttpResponse<String> response = Unirest.delete("https://api.trello.com/1/cards/" + cardId)
                .queryString("key", API_KEY)
                .queryString("token", API_TOKEN)
                .header("Accept", "application/json")
                .asString();

        if (response.getStatus() == 200 || response.getStatus() == 204) {
            System.out.println("✅ Carte Trello supprimée avec succès.");
            return true;
        } else {
            System.err.println("❌ Erreur lors de la suppression de la carte Trello : " + response.getStatus() + " - " + response.getStatusText());
            return false;
        }
    }


    public static String getCardIdByName(String cardName) throws UnirestException {
        for (StatutReservation statut : StatutReservation.values()) {
            HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/lists/" + getTrelloListId(statut) + "/cards")
                    .queryString("key", API_KEY)
                    .queryString("token", API_TOKEN)
                    .header("Accept", "application/json")
                    .asJson();

            JSONArray cards = response.getBody().getArray();
            for (int i = 0; i < cards.length(); i++) {
                JSONObject card = cards.getJSONObject(i);
                if (card.getString("name").equals(cardName)) {
                    return card.getString("id"); // ✅ Retourne l'ID unique de la carte
                }
            }
        }
        return null; // ❌ Aucun ID trouvé
    }

    public static List<String> getAllCardNames() throws UnirestException {
        List<String> cardNames = new ArrayList<>();
        for (StatutReservation statut : StatutReservation.values()) {
            HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/lists/" + getTrelloListId(statut) + "/cards")
                    .queryString("key", API_KEY)
                    .queryString("token", API_TOKEN)
                    .header("Accept", "application/json")
                    .asJson();

            JSONArray cards = response.getBody().getArray();
            for (int i = 0; i < cards.length(); i++) {
                JSONObject card = cards.getJSONObject(i);
                cardNames.add(card.getString("name"));
            }
        }
        return cardNames;
    }






    public static JsonNode getCardsByList(StatutReservation statut) throws UnirestException {
        String listId = getListIdByStatut(statut); // Utilisation du getter public

        HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/lists/" + listId + "/cards")
                .queryString("key", API_KEY)
                .queryString("token", API_TOKEN)
                .header("Accept", "application/json")
                .asJson();

        return response.getBody();
    }



}