package services;

import entities.Hotel;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHotel implements IService<Hotel> {

    private static ServiceHotel instance;

    private ServiceHotel() {}

    public static ServiceHotel getInstance() {
        if (instance == null) {
            instance = new ServiceHotel();
        }
        return instance;
    }

    // ✅ Ajouter un hôtel
    @Override
    public void ajouterhotel(Hotel hotel) throws SQLException {
        String query = "INSERT INTO hotel (nom, prix_nuit, image_hotel, description, tel, email_contact, adresse, est_disponible) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, hotel.getNom());
            stmt.setDouble(2, hotel.getPrix_nuit());
            stmt.setString(3, hotel.getImage_hotel());
            stmt.setString(4, hotel.getDescription());
            stmt.setString(5, hotel.getTel());
            stmt.setString(6, hotel.getEmail_contact());
            stmt.setString(7, hotel.getAdresse());
            stmt.setBoolean(8, hotel.isEst_disponible());

            stmt.executeUpdate();
        }
    }

    // ✅ Modifier un hôtel
    @Override
    public void modifierhotel(Hotel hotel) throws SQLException {
        String query = "UPDATE hotel SET prix_nuit = ?, adresse = ?, description = ?, tel = ?, email_contact = ?, est_disponible = ? WHERE id_hotel = ?";

        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setDouble(1, hotel.getPrix_nuit());
            pstmt.setString(2, hotel.getAdresse());
            pstmt.setString(3, hotel.getDescription());
            pstmt.setString(4, hotel.getTel());
            pstmt.setString(5, hotel.getEmail_contact());
            pstmt.setBoolean(6, hotel.isEst_disponible());
            pstmt.setInt(7, hotel.getId_hotel());

            pstmt.executeUpdate();
        }
    }

    // ✅ Supprimer un hôtel par ID
    @Override
    public void supprimerhotel(int id) throws SQLException {
        String query = "DELETE FROM hotel WHERE id_hotel = ?";

        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // ✅ Supprimer un hôtel par nom
    public boolean supprimerHotelParNom(String nom) {
        String query = "DELETE FROM hotel WHERE nom = ?";

        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, nom);
            return pstmt.executeUpdate() > 0; // Retourne true si suppression réussie

        } catch (SQLException e) {
            System.out.println("Erreur SQL lors de la suppression de l'hôtel : " + e.getMessage());
            return false;
        }
    }

    // ✅ Trouver un hôtel par ID
    @Override
    public Hotel trouverParId(int id) throws SQLException {
        String query = "SELECT * FROM hotel WHERE id_hotel = ?";

        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Hotel(
                        rs.getInt("id_hotel"),
                        rs.getString("nom"),
                        rs.getDouble("prix_nuit"),
                        rs.getBoolean("est_disponible"),
                        rs.getString("adresse"),
                        rs.getString("description"),
                        rs.getString("tel"),
                        rs.getString("email_contact"),
                        rs.getString("image_hotel")
                );
            }
        }
        return null; // Aucun hôtel trouvé
    }

    // ✅ Afficher tous les hôtels
    @Override
    public List<Hotel> afficherhotel() throws SQLException {
        String query = "SELECT * FROM hotel";
        List<Hotel> hotels = new ArrayList<>();

        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                hotels.add(new Hotel(
                        rs.getInt("id_hotel"),
                        rs.getString("nom"),
                        rs.getDouble("prix_nuit"),
                        rs.getBoolean("est_disponible"),
                        rs.getString("adresse"),
                        rs.getString("description"),
                        rs.getString("tel"),
                        rs.getString("email_contact"),
                        rs.getString("image_hotel")
                ));
            }
        }
        return hotels;
    }

    // ✅ Lister les hôtels (même que afficherhotel())
    @Override
    public List<Hotel> lister() throws SQLException {
        return afficherhotel();
    }
    public List<Hotel> searchHotelsByName(String name) throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        String query = "SELECT * FROM hotel WHERE nom LIKE ?";

        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + name + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    hotels.add(mapResultSetToHotel(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error searching hotels by name: " + e.getMessage(), e); // Re-throw the exception
        }
        return hotels;
    }

    //Helper function to map ResultSet to Hotel Object
    private Hotel mapResultSetToHotel(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getInt("id_hotel"),
                rs.getString("nom"),
                rs.getDouble("prix_nuit"),
                rs.getBoolean("est_disponible"),
                rs.getString("adresse"),
                rs.getString("description"),
                rs.getString("tel"),
                rs.getString("email_contact"),
                rs.getString("image_hotel")
        );
    }


}