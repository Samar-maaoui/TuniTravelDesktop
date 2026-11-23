package services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import entities.*;
import utils.MyDatabase;

public class c_reservationvol implements reservationvole<reservationvol> {
    Connection connection;

    public c_reservationvol() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(reservationvol reservationvol) throws SQLException {
        String req = "INSERT INTO reservationvol (vol_id, classe_service, utilisateur_id) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setInt(1, reservationvol.getId_vol());
            preparedStatement.setString(2, reservationvol.getClasse_service().name());
            preparedStatement.setInt(3, reservationvol.getUtilisateur().getIdUtilisateur());
            preparedStatement.executeUpdate();
            System.out.println("✅ Réservation ajoutée avec succès !");
        }
    }

    @Override
    public void modifier(reservationvol reservationvol) throws SQLException {
        String req = "UPDATE reservationvol SET classe_service=? WHERE vol_id=? AND utilisateur_id=?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setString(1, reservationvol.getClasse_service().name());
            preparedStatement.setInt(2, reservationvol.getId_vol());
            preparedStatement.setInt(3, reservationvol.getUtilisateur().getIdUtilisateur());

            int rowsUpdated = preparedStatement.executeUpdate();
            System.out.println(rowsUpdated > 0 ? "✅ Réservation modifiée avec succès !" : "❌ Aucune réservation trouvée avec cet ID.");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Non implémenté
    }

    public void supprimer(reservationvol reservation) throws SQLException {
        String sql = "DELETE FROM reservationvol WHERE id_reservation = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, reservation.getId_reservation());
            preparedStatement.executeUpdate();
            System.out.println("🗑️ Réservation supprimée avec succès !");
        }
    }

    @Override
    public List<reservationvol> afficher() throws SQLException {
        List<reservationvol> reservations = new ArrayList<>();
        String req = "SELECT rv.id_reservation, rv.classe_service, rv.utilisateur_id, " +
                "v.id_vol, v.disponible, v.numero_vol, v.compagnie, v.aero_depart, v.aero_arrivee, v.prix, v.date_depart, v.date_arrivee " +
                "FROM reservationvol rv JOIN vol v ON rv.vol_id = v.id_vol";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(req)) {

            while (resultSet.next()) {
                vol v = new vol(
                        resultSet.getInt("id_vol"),
                        resultSet.getInt("disponible"),
                        resultSet.getString("numero_vol"),
                        resultSet.getString("compagnie"),
                        resultSet.getString("aero_depart"),
                        resultSet.getString("aero_arrivee"),
                        resultSet.getDouble("prix"),
                        resultSet.getDate("date_depart"),
                        resultSet.getDate("date_arrivee")
                );

                reservationvol.ClasseService classeService =
                        reservationvol.ClasseService.valueOf(resultSet.getString("classe_service").toUpperCase());

                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setIdUtilisateur(resultSet.getInt("utilisateur_id"));

                reservationvol res = new reservationvol(
                        resultSet.getInt("id_reservation"),
                        resultSet.getInt("id_vol"),
                        classeService,
                        utilisateur
                );

                res.setVolDetails(v);
                reservations.add(res);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'affichage des réservations : " + e.getMessage());
        }

        System.out.println("🔍 Total réservations récupérées : " + reservations.size());
        return reservations;
    }

    public int findidbynumvol(int id) throws SQLException {
        String req = "SELECT id_vol FROM vol WHERE numero_vol=?";
        try (PreparedStatement statement = connection.prepareStatement(req)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_vol");
            }
        }
        return -1;
    }

    public int getLastInsertedVolId() throws SQLException {
        String req = "SELECT id_vol FROM vol ORDER BY id_vol DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(req);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id_vol");
            }
        }
        return -1;
    }

    public List<reservationvol> getAllReservations() throws SQLException {
        List<reservationvol> reservations = new ArrayList<>();
        String query = "SELECT rv.id_reservation, rv.classe_service, rv.utilisateur_id, " +
                "v.id_vol, v.disponible, v.numero_vol, v.compagnie, v.aero_depart, v.aero_arrivee, v.prix, v.date_depart, v.date_arrivee " +
                "FROM reservationvol rv JOIN vol v ON rv.vol_id = v.id_vol";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                vol v = new vol(
                        resultSet.getInt("id_vol"),
                        resultSet.getInt("disponible"),
                        resultSet.getString("numero_vol"),
                        resultSet.getString("compagnie"),
                        resultSet.getString("aero_depart"),
                        resultSet.getString("aero_arrivee"),
                        resultSet.getDouble("prix"),
                        resultSet.getDate("date_depart"),
                        resultSet.getDate("date_arrivee")
                );

                reservationvol.ClasseService classeService =
                        reservationvol.ClasseService.valueOf(resultSet.getString("classe_service").toUpperCase());

                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setIdUtilisateur(resultSet.getInt("utilisateur_id"));

                reservationvol res = new reservationvol(
                        resultSet.getInt("id_reservation"),
                        resultSet.getInt("id_vol"),
                        classeService,
                        utilisateur
                );

                res.setVolDetails(v);
                reservations.add(res);
            }
        }

        if (reservations.isEmpty()) {
            System.out.println("⚠️ La liste des réservations est vide.");
        } else {
            System.out.println("📋 Réservations récupérées : " + reservations.size());
        }

        return reservations;
    }
}
