package tests;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import services.TrelloService;

import java.util.List;
import java.util.Scanner;

public class TrelloTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // 🔎 Étape 1 : Lister toutes les cartes disponibles
            System.out.println("\n📋 Liste des cartes Trello disponibles :");
            List<String> cardNames = TrelloService.getAllCardNames();

            if (cardNames.isEmpty()) {
                System.out.println("❌ Aucune carte trouvée dans Trello !");
                return;
            }

            for (String cardName : cardNames) {
                System.out.println("➡ " + cardName);
            }

            // 🔎 Étape 2 : Demander un nom de carte à supprimer
            System.out.print("\n🗑️ Entrez le nom exact de la carte à supprimer : ");
            String selectedCard = scanner.nextLine();

            // 🔎 Étape 3 : Trouver l'ID de la carte
            String cardId = TrelloService.getCardIdByName(selectedCard);
            if (cardId == null || cardId.isEmpty()) {
                System.out.println("❌ ERREUR : Aucune correspondance trouvée pour la carte : " + selectedCard);
                return;
            }

            System.out.println("✅ ID trouvé pour " + selectedCard + " : " + cardId);

            // 🔎 Étape 4 : Suppression de la carte
            boolean success = TrelloService.deleteTrelloCard(cardId);
            if (success) {
                System.out.println("✅ Carte supprimée avec succès !");
            } else {
                System.out.println("❌ Échec de la suppression !");
            }

        } catch (UnirestException e) {
            System.err.println("❌ ERREUR : Problème lors de la connexion à Trello.");
            e.printStackTrace();
        }

        scanner.close();
    }
}
