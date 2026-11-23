package controller;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.TrelloService;
import entities.StatutReservation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrelloController {

    @FXML
    private ListView<String> listeEnAttente;
    @FXML
    private ListView<String> listeConfirmees;
    @FXML
    private ListView<String> listeAnnulees;
    @FXML
    private Button syncButton;

    private final Map<String, String> cardIdMap = new HashMap<>(); // 📌 Associe nom → ID
    private String currentCardId = null; // 📌 Stocke l'ID de la carte sélectionnée

    @FXML
    public void initialize() {
        try {
            loadReservations();
        } catch (UnirestException e) {
            showAlert("Erreur", "Impossible de charger les réservations depuis Trello.");
            e.printStackTrace();
        }

        setupSelectionListeners();
    }

    /**
     * Charge les réservations et stocke leurs IDs.
     */
    private void loadReservations() throws UnirestException {
        cardIdMap.clear(); // 🔄 Réinitialiser les associations ID -> Carte

        listeEnAttente.setItems(getReservationsByStatut(StatutReservation.EN_ATTENTE));
        listeConfirmees.setItems(getReservationsByStatut(StatutReservation.CONFIRMEE));
        listeAnnulees.setItems(getReservationsByStatut(StatutReservation.ANNULEE));
    }

    private ObservableList<String> getReservationsByStatut(StatutReservation statut) throws UnirestException {
        ObservableList<String> reservations = FXCollections.observableArrayList();
        JsonNode response = TrelloService.getCardsByList(statut);

        JSONArray cards = response.getArray();
        for (int i = 0; i < cards.length(); i++) {
            JSONObject card = cards.getJSONObject(i);
            String name = card.getString("name");
            String desc = card.getString("desc");
            String cardId = card.getString("id"); // 📌 Récupérer l'ID de la carte

            // 📌 Extraire l'email du champ description
            String email = "Inconnu";
            String[] descriptionLines = desc.split("\n");
            if (descriptionLines.length > 1) {
                email = descriptionLines[1].replace("📧 Email: ", "").trim();
            }

            String displayText = name + " | " + email;
            reservations.add(displayText);

            cardIdMap.put(displayText, cardId); // 📌 Associer l'affichage à l'ID
        }
        return reservations;
    }

    /**
     * Gère la sélection d'une carte dans les listes et récupère son ID.
     */
    private void setupSelectionListeners() {
        listeEnAttente.setOnMouseClicked(event -> selectCardFromList(listeEnAttente));
        listeConfirmees.setOnMouseClicked(event -> selectCardFromList(listeConfirmees));
        listeAnnulees.setOnMouseClicked(event -> selectCardFromList(listeAnnulees));
    }

    private void selectCardFromList(ListView<String> listView) {
        String selectedCard = listView.getSelectionModel().getSelectedItem();
        if (selectedCard != null && cardIdMap.containsKey(selectedCard)) {
            currentCardId = cardIdMap.get(selectedCard); // 📌 Récupérer l'ID de la carte sélectionnée
            System.out.println("🔍 Carte sélectionnée : " + selectedCard + " (ID: " + currentCardId + ")");
        } else {
            currentCardId = null;
            System.out.println("❌ Aucune carte valide sélectionnée.");
        }
    }

    /**
     * Supprime la carte Trello sélectionnée.
     */
    @FXML
    private void deleteCard() {
        if (currentCardId == null || currentCardId.isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner une réservation à supprimer !");
            return;
        }

        try {
            System.out.println("🗑️ Suppression de la carte avec ID : " + currentCardId);

            boolean success = TrelloService.deleteTrelloCard(currentCardId);
            if (success) {
                showAlert("Succès", "Carte supprimée avec succès !");
                loadReservations(); // 🔄 Rafraîchir l'affichage
                currentCardId = null; // Réinitialiser après suppression
            } else {
                showAlert("Erreur", "Impossible de supprimer la carte Trello !");
            }
        } catch (UnirestException e) {
            showAlert("Erreur", "Une erreur s'est produite lors de la suppression !");
            e.printStackTrace();
        }
    }

    /**
     * Synchronise les réservations avec Trello.
     */
    @FXML
    private void synchroniserReservations() {
        try {
            TrelloService.synchroniserReservationsAvecTrello();
            loadReservations(); // 🔄 Recharger les listes après la synchro
            showAlert("Succès", "Synchronisation avec Trello terminée !");
        } catch (UnirestException e) {
            showAlert("Erreur", "Impossible de synchroniser avec Trello.");
            e.printStackTrace();
        }
    }

    /**
     * Affiche une boîte de dialogue d'alerte.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
