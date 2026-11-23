package services;

import entities.ReservationHotel;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservationHotel {
    private Connection con;

    public ServiceReservationHotel() {
        con = MyDatabase.getInstance().getConnection();
        if (con == null) {
            System.err.println("❌ Erreur : La connexion à la base de données est NULL !");
        } else {
            System.out.println("✅ Connexion à la base de données réussie !");
        }
    }

    /**
     * Vérifie si la connexion est fermée et la rouvre si nécessaire.
     */
    private void checkConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            System.out.println("🔄 Réouverture de la connexion à la base de données...");
            con = MyDatabase.getInstance().getConnection();
        }
    }

    /**
     * Ajout d'une réservation en s'assurant que l'hôtel existe.
     */
    public void ajouterReservation(ReservationHotel reservation) {
        try {
            checkConnection(); // 🔄 Vérification avant toute requête

            String queryCheckHotelExistence = "SELECT COUNT(*) FROM hotel WHERE id_hotel = ?";
            try (PreparedStatement pstmtCheck = con.prepareStatement(queryCheckHotelExistence)) {
                pstmtCheck.setInt(1, reservation.getId_hotel());
                ResultSet rs = pstmtCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    String query = "INSERT INTO reservationhotel (hotel_id, check_in_date, check_out_date, type_chambre) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = con.prepareStatement(query)) {
                        pstmt.setInt(1, reservation.getId_hotel());
                        pstmt.setDate(2, reservation.getCheck_in_date());
                        pstmt.setDate(3, reservation.getCheck_out_date());
                        pstmt.setString(4, reservation.getType_chambre());
                        pstmt.executeUpdate();
                        System.out.println("✅ Réservation ajoutée avec succès.");
                    }
                } else {
                    System.out.println("❌ Erreur : L'hôtel n'existe pas.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout de la réservation : " + e.getMessage());
        }
    }

    /**
     * Récupération des réservations associées à un hôtel donné.
     */
    public List<ReservationHotel> getReservationsParHotel(int idHotel) {
        List<ReservationHotel> reservations = new ArrayList<>();
        try {
            checkConnection(); // 🔄 Vérification avant toute requête

            String query = "SELECT * FROM reservationhotel WHERE hotel_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setInt(1, idHotel);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    ReservationHotel reservation = new ReservationHotel(
                            rs.getInt("id_reservation"),
                            rs.getInt("hotel_id"),
                            rs.getDate("check_in_date"),
                            rs.getDate("check_out_date"),
                            rs.getString("type_chambre")
                    );
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des réservations : " + e.getMessage());
        }
        return reservations;
    }

    /**
     * Suppression d'une réservation.
     */
    public void supprimerReservation(int idReservation) {
        try {
            checkConnection(); // 🔄 Vérification avant toute requête

            String query = "DELETE FROM reservationhotel WHERE id_reservation = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setInt(1, idReservation);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Réservation supprimée avec succès.");
                } else {
                    System.out.println("⚠️ Aucune réservation trouvée avec cet ID.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression de la réservation : " + e.getMessage());
        }
    }

    /**
     * Modification d'une réservation.
     */
    public boolean modifierReservation(ReservationHotel reservation) {
        try {
            checkConnection(); // 🔄 Vérification avant toute requête

            String query = "UPDATE reservationhotel SET check_in_date = ?, check_out_date = ?, type_chambre = ? WHERE id_reservation = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setDate(1, reservation.getCheck_in_date());
                pstmt.setDate(2, reservation.getCheck_out_date());
                pstmt.setString(3, reservation.getType_chambre());
                pstmt.setInt(4, reservation.getId_reservation());
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification de la réservation : " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérification de l'existence d'un hôtel dans la base de données.
     */
    public boolean hotelExiste(int idHotel) {
        try {
            checkConnection(); // 🔄 Vérification avant toute requête

            String query = "SELECT COUNT(*) FROM hotel WHERE id_hotel = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, idHotel);
                ResultSet rs = ps.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la vérification de l'hôtel : " + e.getMessage());
        }
        return false;
    }
}
