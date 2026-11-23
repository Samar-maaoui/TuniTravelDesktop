package services;

import entities.reservationEvenement;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservationEvenement {
    private Connection connection;

    public ServiceReservationEvenement() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public boolean ajouterReservation(reservationEvenement reservation) {
        String queryInsert = "INSERT INTO reservationevenement (evenement_id, id_utilisateur, nombre_places, date_reservation) VALUES (?, ?, ?, ?)";
        String queryUpdateCapacity = "UPDATE evenements SET capacite = capacite - ? WHERE id_evenement = ? AND capacite >= ?";

        try {
            connection.setAutoCommit(false);

            PreparedStatement psInsert = connection.prepareStatement(queryInsert);
            psInsert.setInt(1, reservation.getIdEvenement());
            psInsert.setInt(2, reservation.getIdUtilisateur());
            psInsert.setInt(3, reservation.getNbPlaces());
            psInsert.setTimestamp(4, java.sql.Timestamp.valueOf(reservation.getDateInscription()));

            int rowsAffected = psInsert.executeUpdate();

            if (rowsAffected > 0) {
                PreparedStatement psUpdate = connection.prepareStatement(queryUpdateCapacity);
                psUpdate.setInt(1, reservation.getNbPlaces());
                psUpdate.setInt(2, reservation.getIdEvenement());
                psUpdate.setInt(3, reservation.getNbPlaces());

                int rowsUpdated = psUpdate.executeUpdate();

                if (rowsUpdated > 0) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }

            connection.rollback();
            return false;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getAllReservationsForAdmin() {
        List<String> reservations = new ArrayList<>();
        String query = "SELECT r.id_reservation, e.nom AS nom_evenement, u.email, u.cin, r.nombre_places, r.date_reservation " +
                "FROM reservationevenement r " +
                "JOIN evenements e ON r.evenement_id = e.id_evenement " +
                "JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                String reservationDetails = "Reservation ID: " + rs.getInt("id_reservation") +
                        ", Event: " + rs.getString("nom_evenement") +
                        ", User Email: " + rs.getString("email") +
                        ", CIN: " + rs.getString("cin") +
                        ", Number of Places: " + rs.getInt("nombre_places") +
                        ", Date: " + rs.getTimestamp("date_reservation");
                reservations.add(reservationDetails);
            }

            if (rowCount == 0) {
                System.out.println("No reservations found.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getAllReservationsForAdmin: " + e.getMessage());
            e.printStackTrace();
        }
        return reservations;
    }

    public List<String> getReservationsForUser(int userId) {
        List<String> reservations = new ArrayList<>();
        String query = "SELECT e.nom AS nom_evenement, r.nombre_places, r.date_reservation " +
                "FROM reservationevenement r " +
                "JOIN evenements e ON r.evenement_id = e.id_evenement " +
                "WHERE r.id_utilisateur = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String reservationDetails = "Event: " + rs.getString("nom_evenement") +
                            ", Number of Places: " + rs.getInt("nombre_places") +
                            ", Date: " + rs.getTimestamp("date_reservation");
                    reservations.add(reservationDetails);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
}
