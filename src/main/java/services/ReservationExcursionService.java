package services;

import entities.Excursion;
import entities.Guide;
import entities.ReservationExcursion;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationExcursionService implements Services<ReservationExcursion> {
    Connection connection = MyDatabase.getInstance().getConnection();
    public ReservationExcursionService() throws SQLException {
    }

    // ✅ Ajouter une réservation d'excursion
    public void ajouter(ReservationExcursion reservation) throws SQLException {
        String sqlReservation = "INSERT INTO reservation (utilisateur_id , date_reservation, statut, montant_total) VALUES (?, ?, ?, ?)";
        String sqlReservationExcursion = "INSERT INTO reservationexcursion (excursion_id , nbr_place_reservee, id_reservation ) VALUES (?, ?, ?)";
        String sqlUpdatePlaces = "UPDATE excursion SET nombre_max_participants = nombre_max_participants - ? WHERE id_excursion = ? AND nombre_max_participants >= ?";

        try (PreparedStatement statementReservation = connection.prepareStatement(sqlReservation, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement statementReservationExcursion = connection.prepareStatement(sqlReservationExcursion);
             PreparedStatement statementUpdatePlaces = connection.prepareStatement(sqlUpdatePlaces)) {

            // 🔹 Insertion dans la table reservation
            statementReservation.setInt(1, reservation.getUtilisateurId());
            statementReservation.setTimestamp(2, new Timestamp(reservation.getDateReservation().getTime()));

            statementReservation.setString(3, reservation.getStatut().toString());
            statementReservation.setDouble(4, reservation.getMontantTotal());

            int rowsAffected = statementReservation.executeUpdate();

            if (rowsAffected > 0) {
                // 🔹 Récupérer le ID de la réservation nouvellement insérée
                ResultSet generatedKeys = statementReservation.getGeneratedKeys();
                int idReservation = 0;
                if (generatedKeys.next()) {
                    idReservation = generatedKeys.getInt(1);
                }

                // 🔹 Insertion dans la table reservationexcursion
                statementReservationExcursion.setInt(1, reservation.getExcursion().getIdExcursion());
                statementReservationExcursion.setInt(2, reservation.getNbrPlaceReservee());
                statementReservationExcursion.setInt(3, idReservation);
                statementReservationExcursion.executeUpdate();

                // 🔹 Mise à jour du nombre de places disponibles
                statementUpdatePlaces.setInt(1, reservation.getNbrPlaceReservee());
                statementUpdatePlaces.setInt(2, reservation.getExcursion().getIdExcursion());
                statementUpdatePlaces.setInt(3, reservation.getNbrPlaceReservee());
                int placesAffected = statementUpdatePlaces.executeUpdate();

                if (placesAffected > 0) {
                    System.out.println("✅ Réservation ajoutée et nombre de places mis à jour avec succès.");
                } else {
                    System.out.println("⚠️ Erreur : Pas assez de places disponibles dans l'excursion.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("❌ Erreur lors de l'ajout de la réservation.");
        }
    }

    // ✅ Lire toutes les réservations d'excursion
    public List<ReservationExcursion> lire() throws SQLException {
        List<ReservationExcursion> reservations = new ArrayList<>();
        String sql = "SELECT id_reservation, excursion_id, nbr_place_reservee, statut, montant_total FROM reservationexcursion";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            Map<Integer, Excursion> excursionCache = new HashMap<>();
            ExcursionService excursionService = new ExcursionService();

            while (resultSet.next()) {
                int idReservation = resultSet.getInt("id_reservation");
                int excursionId = resultSet.getInt("excursion_id");
                int nbrPlaceReservee = resultSet.getInt("nbr_place_reservee");
                String statut = resultSet.getString("statut");
                Double montantTotal = resultSet.getDouble("montant_total");

                Excursion excursion = excursionCache.computeIfAbsent(excursionId, id -> {
                    try {
                        return excursionService.trouverParId(id);
                    } catch (SQLException e) {
                        System.out.println("❌ Excursion introuvable pour ID: " + id);
                        return null;
                    }
                });

                reservations.add(new ReservationExcursion(idReservation, excursion, statut, nbrPlaceReservee, montantTotal));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("❌ Erreur lors de la lecture de toutes les réservations.");
        }
        return reservations;
    }


    public List<ReservationExcursion> lireParUtilisateur(int idUtilisateur) throws SQLException {
        List<ReservationExcursion> reservations = new ArrayList<>();
        String sql = "SELECT id_reservation, excursion_id, nbr_place_reservee, statut, montant_total " +
                "FROM reservationexcursion WHERE utilisateur_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUtilisateur);

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Integer, Excursion> excursionCache = new HashMap<>();
                ExcursionService excursionService = new ExcursionService();

                while (resultSet.next()) {
                    int idReservation = resultSet.getInt("id_reservation");
                    int excursionId = resultSet.getInt("excursion_id");
                    int nbrPlaceReservee = resultSet.getInt("nbr_place_reservee");
                    String statutStr = resultSet.getString("statut");
                    Double montantTotal = resultSet.getDouble("montant_total");

                    Excursion excursion = excursionCache.computeIfAbsent(excursionId, id -> {
                        try {
                            return excursionService.trouverParId(id);
                        } catch (SQLException e) {
                            System.out.println("❌ Excursion ID " + id + " introuvable.");
                            return null;
                        }
                    });

                    // ⚠️ Assurez-vous que ReservationExcursion accepte un enum ou String selon ton modèle
                    reservations.add(new ReservationExcursion(idReservation, excursion, statutStr, nbrPlaceReservee, montantTotal));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("❌ Erreur lors de la lecture des réservations par utilisateur.", e);
        }

        return reservations;
    }



    // ✅ Supprimer toutes les réservations liées à une excursion
    public void supprimerReservationsParExcursion(int idExcursion) throws SQLException {
        String query = "DELETE FROM reservationexcursion WHERE excursion_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idExcursion);
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Toutes les réservations liées à l'excursion ID " + idExcursion + " ont été supprimées.");
            } else {
                System.out.println("⚠️ Aucune réservation trouvée pour cette excursion.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("❌ Erreur lors de la suppression des réservations.");
        }
    }

    // ✅ Modifier une réservation d'excursion
    public boolean modifier(ReservationExcursion reservation) throws SQLException {
        String sql = "UPDATE reservationexcursion SET excursion_id = ?, nbr_place_reservee = ? WHERE id_reservation = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservation.getExcursion().getIdExcursion());
            statement.setInt(2, reservation.getNbrPlaceReservee());
            statement.setInt(3, reservation.getIdReservation());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Réservation modifiée avec succès.");
                return true;
            } else {
                System.out.println("⚠️ Aucune réservation trouvée avec cet identifiant.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("❌ Erreur lors de la modification de la réservation.");
        }
    }

    // ✅ Supprimer une réservation d'excursion
    public void supprimer(int idReservation) throws SQLException {
        String selectExcursionSql = "SELECT excursion_id, nbr_place_reservee FROM reservationexcursion WHERE id_reservation = ?";
        String deleteExcursionSql = "DELETE FROM reservationexcursion WHERE id_reservation = ?";
        String deleteReservationSql = "DELETE FROM reservation WHERE id_reservation = ?";
        String updateExcursionSql = "UPDATE excursion SET nombre_max_participants = nombre_max_participants + ? WHERE id_excursion = ?";

        try {
            connection.setAutoCommit(false); // Démarrer une transaction

            try (PreparedStatement selectExcursionStmt = connection.prepareStatement(selectExcursionSql);
                 PreparedStatement deleteExcursionStmt = connection.prepareStatement(deleteExcursionSql);
                 PreparedStatement deleteReservationStmt = connection.prepareStatement(deleteReservationSql);
                 PreparedStatement updateExcursionStmt = connection.prepareStatement(updateExcursionSql)) {

                // Étape 1: Récupérer l'ID de l'excursion et le nombre de places réservées
                selectExcursionStmt.setInt(1, idReservation);
                ResultSet rs = selectExcursionStmt.executeQuery();

                if (rs.next()) {
                    int idExcursion = rs.getInt("excursion_id");
                    int nbrPlaceReservee = rs.getInt("nbr_place_reservee");

                    // Étape 2: Mettre à jour le nombre de places disponibles dans la table excursion
                    updateExcursionStmt.setInt(1, nbrPlaceReservee);
                    updateExcursionStmt.setInt(2, idExcursion);
                    updateExcursionStmt.executeUpdate();

                    // Étape 3: Supprimer de reservationexcursion et reservation
                    deleteExcursionStmt.setInt(1, idReservation);
                    deleteExcursionStmt.executeUpdate();

                    deleteReservationStmt.setInt(1, idReservation);
                    int rowsDeleted = deleteReservationStmt.executeUpdate();

                    if (rowsDeleted > 0) {
                        connection.commit(); // Valider la transaction
                        System.out.println("✅ Réservation supprimée avec succès et nombre de places mis à jour.");
                    } else {
                        connection.rollback(); // Annuler si aucune réservation trouvée
                        System.out.println("⚠️ Aucune réservation trouvée avec cet identifiant.");
                    }
                } else {
                    System.out.println("⚠️ Aucun enregistrement trouvé avec cet ID de réservation.");
                    connection.rollback();
                }
            }
        } catch (SQLException e) {
            connection.rollback(); // Annuler en cas d'erreur
            e.printStackTrace();
            throw new SQLException("❌ Erreur lors de la suppression de la réservation.");
        } finally {
            connection.setAutoCommit(true); // Restaurer le mode auto-commit
        }
    }



    @Override
    public List<Excursion> lister() throws SQLException {
        List<Excursion> excursions = new ArrayList<>();
        String req = "SELECT e.*, g.nom AS nom_guide, g.prenom AS prenom_guide " +
                "FROM excursion e " +
                "LEFT JOIN guide g ON e.id_guide = g.id_guide";

        try (PreparedStatement statement = connection.prepareStatement(req);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                // Création de l'objet Excursion
                Excursion excursion = new Excursion();
                excursion.setIdExcursion(rs.getInt("excursion_id"));
                excursion.setNom(rs.getString("nom"));
                excursion.setLieu(rs.getString("lieu"));
                excursion.setPrix(rs.getDouble("prix"));
                excursion.setDateDepart(rs.getDate("date_depart").toLocalDate());
                excursion.setDateRetour(rs.getDate("date_retour").toLocalDate());
                excursion.setDescription(rs.getString("description"));
                excursion.setImage(rs.getString("image"));
                excursion.setNbPlace(rs.getInt("placesDispos"));

                // Création de l'objet Guide (s'il existe)
                Guide guide = null;
                if (rs.getInt("id_guide") != 0) {
                    guide = new Guide(
                            rs.getInt("id_guide"),
                            rs.getString("nom_guide"),
                            rs.getString("prenom_guide")
                    );
                }
                excursion.setGuide(guide);

                excursions.add(excursion);
            }
        }
        return excursions;
    }

}
