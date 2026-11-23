package services;

import entities.Evenement;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;




public class ServiceEvenement implements Ievenement {
    private static Connection connection;

    public ServiceEvenement() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouterEvenement(Evenement evenement) {
        String sql = "INSERT INTO evenements (nom, lieu, date_evenement, capacite, type, organisateur, description, prix) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, evenement.getNom());
            stmt.setString(2, evenement.getLieu());
            stmt.setString(3, evenement.getDateEvenement());
            stmt.setInt(4, evenement.getCapacite());
            stmt.setString(5, evenement.getType());
            stmt.setString(6, evenement.getOrganisateur());
            stmt.setString(7, evenement.getDescription());
            stmt.setDouble(8, evenement.getPrix());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    evenement.setIdEvenement(generatedKeys.getInt(1));
                }
                System.out.println("Event added successfully: " + evenement.getNom());
            } else {
                System.out.println("Failed to add event.");
            }

        } catch (SQLException e) {
            System.err.println("Error inserting event: " + e.getMessage());
        }
    }

    @Override

    public List<Evenement> getEvenements() {
        List<Evenement> evenements = new ArrayList<>();
        String sql = "SELECT * FROM evenements";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id_evenement");
                String nom = rs.getString("nom");
                String lieu = rs.getString("lieu");
                String dateEvenement = rs.getDate("date_evenement") != null ? rs.getDate("date_evenement").toString() : null;
                String type = rs.getString("type");
                int capacite = rs.getInt("capacite");
                String organisateur = rs.getString("organisateur");
                String description = rs.getString("description");
                double prix = rs.getDouble("prix");

                // Debugging output
                System.out.println("Fetched Event: ID=" + id + ", Nom=" + nom + ", Lieu=" + lieu + ", Date=" + dateEvenement + ", Type=" + type + ", Prix=" + prix);

                Evenement e = new Evenement(id, nom, lieu, dateEvenement, capacite, type, organisateur, description, prix);
                evenements.add(e);
            }

            System.out.println("Total Events Retrieved: " + evenements.size());

        } catch (SQLException e) {
            System.err.println("Error fetching events: " + e.getMessage());
        }
        return evenements;
    }

    public static Evenement getEvenementById(int evenementId) {
        Evenement evenement = null;
        // Ensure the table name and column names match your database schema
        String query = "SELECT * FROM Evenements WHERE id_evenement = ?";  // Adjust column name 'id_evenement' as per your DB schema

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, evenementId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Create the Evenement object and populate its fields from the result set
                evenement = new Evenement(
                        resultSet.getInt("id_evenement"),   // Adjust column name
                        resultSet.getString("nom"),
                        resultSet.getString("lieu"),
                        resultSet.getString("date_evenement"),
                        resultSet.getInt("capacite"),
                        resultSet.getString("type"),
                        resultSet.getString("organisateur"),
                        resultSet.getString("description"),
                        resultSet.getDouble("prix")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Log or handle the exception as needed
        }

        return evenement;
    }


    @Override
    public void modifierEvenement(Evenement evenement) {
        String sql = "UPDATE evenements SET nom=?, lieu=?, date_evenement=?, capacite=?, type=?, organisateur=?, description=?, prix=? WHERE id_evenement=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, evenement.getNom());
            stmt.setString(2, evenement.getLieu());
            stmt.setString(3, evenement.getDateEvenement());
            stmt.setInt(4, evenement.getCapacite());
            stmt.setString(5, evenement.getType());
            stmt.setString(6, evenement.getOrganisateur());
            stmt.setString(7, evenement.getDescription());
            stmt.setDouble(8, evenement.getPrix());
            stmt.setInt(9, evenement.getIdEvenement());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Event updated successfully: " + evenement.getNom());
            } else {
                System.out.println("No event found with ID: " + evenement.getIdEvenement());
            }

        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
        }
    }

    @Override
    public void supprimerEvenement(int idEvenement) {
        String sql = "DELETE FROM evenements WHERE id_evenement=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idEvenement);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Event deleted successfully (ID: " + idEvenement + ")");
            } else {
                System.out.println("No event found with ID: " + idEvenement);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
        }
    }
    public List<Evenement> getEvenementsForMonth(int year, int month) {
        List<Evenement> evenementsForMonth = new ArrayList<>();
        String sql = "SELECT * FROM evenements WHERE YEAR(date_evenement) = ? AND MONTH(date_evenement) = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_evenement");
                String nom = rs.getString("nom");
                String lieu = rs.getString("lieu");
                String dateEvenement = rs.getDate("date_evenement") != null ? rs.getDate("date_evenement").toString() : null;
                String type = rs.getString("type");
                int capacite = rs.getInt("capacite");
                String organisateur = rs.getString("organisateur");
                String description = rs.getString("description");
                double prix = rs.getDouble("prix");

                Evenement e = new Evenement(id, nom, lieu, dateEvenement, capacite, type, organisateur, description, prix);
                evenementsForMonth.add(e);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching events for the month: " + e.getMessage());
        }
        return evenementsForMonth;
    }


}