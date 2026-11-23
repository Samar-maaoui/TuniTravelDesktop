package services;

import entities.Excursion;
import entities.Guide;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuideService implements Services<Guide>  {
    Connection connection = MyDatabase.getInstance().getConnection();

    public GuideService() throws SQLException {
    }
    public List<Guide> getAllGuides() throws SQLException {
        List<Guide> guides = new ArrayList<>();
        String query = "SELECT * FROM guide";
        PreparedStatement ps = connection.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            guides.add(new Guide(rs.getInt("id_guide"), rs.getString("nom"), rs.getString("prenom")));
        }

        return guides;
    }

    public Guide getGuideByName(String nomGuide) throws SQLException {
        String sql = "SELECT id_guide, nom, prenom FROM guide WHERE nom = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomGuide);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Guide(rs.getInt("id_guide"), rs.getString("nom"), rs.getString("prenom"));
            }
        }
        return null;
    }
    public boolean isEmailExist(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM guide WHERE email = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) > 0;
    }

    public boolean isTelephoneExist(String telephone) throws SQLException {
        String query = "SELECT COUNT(*) FROM guide WHERE numero_tel = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, telephone);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) > 0;
    }

    // Méthode pour ajouter un guide
    public void ajouter(Guide guide) throws SQLException {
        String req = "INSERT INTO guide (nom, prenom, langues, numero_tel, email , image) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = connection.prepareStatement(req);

        statement.setString(1, guide.getNom());
        statement.setString(2, guide.getPrenom());
        statement.setString(3, guide.getLangue());
        statement.setString(4, guide.getTelephone());
        statement.setString(5, guide.getEmail());
        statement.setString(6, guide.getImage());


        statement.executeUpdate();
        System.out.println("Guide ajouté avec succès.");
    }
    @Override
    public boolean modifier(Guide guide) throws SQLException {
        // SQL query for updating the guide's information
        String req = "UPDATE guide SET nom = ?, prenom = ?, image = ?, langues = ?, numero_tel = ?, email = ? WHERE id_guide = ?";

        // Use try-with-resources to automatically close the PreparedStatement
        try (PreparedStatement statement = connection.prepareStatement(req)) {

            // Set parameters for the SQL query
            statement.setString(1, guide.getNom());
            statement.setString(2, guide.getPrenom());
            statement.setString(3, guide.getImage());
            statement.setString(4, guide.getLangue());
            statement.setString(5, guide.getTelephone());
            statement.setString(6, guide.getEmail());
            statement.setInt(7, guide.getIdGuide()); // The guide's ID for updating

            // Execute the update query and check how many rows were affected
            int rowsUpdated = statement.executeUpdate();

            // Return true if the guide was successfully updated, otherwise false
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating guide: " + e.getMessage());
            throw e; // Rethrow the exception after logging it
        }
    }

    public void supprimer(Guide guide) throws SQLException {
        String query = "DELETE FROM guide WHERE id_guide = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, guide.getIdGuide());
            preparedStatement.executeUpdate();
        }
    }
    @Override
    public void supprimer(int idGuide) throws SQLException {
        String req = "DELETE FROM guide WHERE id_guide = ?";

        PreparedStatement statement = connection.prepareStatement(req);
        statement.setInt(1, idGuide);

        int rowsDeleted = statement.executeUpdate();
        if (rowsDeleted > 0) {
            System.out.println("Guide supprimé avec succès.");
        } else {
            System.out.println("Aucun guide trouvé avec cet ID.");
        }
    }
    @Override
    public List<Guide> lire() throws SQLException {
        List<Guide> guides = new ArrayList<>();
        String req = "SELECT * FROM guide";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(req);

        while (rs.next()) {
            Guide guide = new Guide();
            guide.setIdGuide(rs.getInt("id_guide"));
            guide.setNom(rs.getString("nom"));
            guide.setPrenom(rs.getString("prenom"));
            guide.setImage(rs.getString("image"));
            guide.setLangue(rs.getString("langues"));
            guide.setTelephone(rs.getString("numero_tel"));

            guide.setEmail(rs.getString("email"));

            guides.add(guide);
        }

        return guides;
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
                excursion.setIdExcursion(rs.getInt("id_excursion"));
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



    public Guide trouverParId(int idGuide) throws SQLException {
        String req = "SELECT * FROM guide WHERE id_guide = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, idGuide);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Guide(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("image"),
                        rs.getString("langues"),
                        rs.getString("numero_tel"),
                        rs.getString("email")
                );
            }
        }
        return null; // Retourne null si aucun guide trouvé
    }



}
