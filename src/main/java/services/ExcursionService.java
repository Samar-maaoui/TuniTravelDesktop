package services;

import entities.ExcursionStat;
import entities.Guide;
import utils.MyDatabase;
import entities.Excursion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExcursionService implements Services<Excursion> {
    Connection connection = MyDatabase.getInstance().getConnection();
    private static ExcursionService instance;

    public ExcursionService() throws SQLException {}

    // ✅ Méthode getInstance() pour récupérer l'unique instance
    public static ExcursionService getInstance() {
        if (instance == null) {
            try {
                instance = new ExcursionService();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de la récupération de l'instance de ExcursionService", e);
            }
        }
        return instance;
    }

    public List<Excursion> filtrerParPrix(double prixMax) throws SQLException {
        List<Excursion> excursions = new ArrayList<>();
        String query = "SELECT e.id_excursion, e.nom, e.lieu, e.prix, e.image, " +
                "g.id_guide, g.nom AS guide_nom, g.prenom AS guide_prenom " + // Correction ici
                "FROM excursion e " +
                "JOIN guide g ON e.id_guide = g.id_guide " +
                "WHERE e.prix <= ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setDouble(1, prixMax);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Guide guide = new Guide(rs.getInt("id_guide"), rs.getString("guide_nom"),
                    rs.getString("guide_prenom"));
            Excursion excursion = new Excursion(rs.getInt("id_excursion"), rs.getString("nom"),
                    rs.getString("lieu"), rs.getDouble("prix"),
                    rs.getString("image"), guide);
            excursions.add(excursion);
        }
        return excursions;
    }

    // ✅ Trouver une excursion par ID
    public Excursion trouverParId(int idExcursion) throws SQLException {
        String sql = "SELECT * FROM excursion WHERE id_excursion = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, idExcursion);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int idGuide = resultSet.getInt("id_guide");
            Guide guide = null;
            if (idGuide != 0) {
                guide = trouverGuideParId(idGuide);
            }

            return new Excursion(
                    resultSet.getInt("id_excursion"),
                    resultSet.getString("nom"),
                    resultSet.getString("lieu"),
                    resultSet.getDouble("prix"),
                    resultSet.getDate("date_depart").toLocalDate(),
                    resultSet.getDate("date_retour").toLocalDate(),
                    resultSet.getString("description"),
                    resultSet.getString("image"),
                    guide,
                    resultSet.getInt("nombre_max_participants"),
                    resultSet.getString("ville_depart")
            );
        }
        return null; // Retourner null si l'excursion n'est pas trouvée
    }
    public String Detail(int id) {
        String sql = "SELECT description FROM excursion WHERE id_excursion = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String description = null;

        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                description = resultSet.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return description;
    }
    public List<String> getAdressesExcursions() {
        List<String> adresses = new ArrayList<>();
        String query = "SELECT lieu FROM excursion"; // Assure-toi que le champ `adresse` existe dans ta table

        try (
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                adresses.add(resultSet.getString("lieu"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des adresses: " + e.getMessage());
        }
        return adresses;
    }
    public List<String> getVilleDepartExcursions() {
        List<String> adresses = new ArrayList<>();
        String query = "SELECT ville_depart FROM excursion"; // Assure-toi que le champ `adresse` existe dans ta table

        try (
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                adresses.add(resultSet.getString("ville_depart"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des ville_depart: " + e.getMessage());
        }
        return adresses;
    }
    public List<ExcursionStat> getReservationsByExcursion() {
        String sql = "SELECT e.id_excursion, e.nom AS excursion_name, " +
                "COUNT(r.id_reservation) AS reservations_count, " +
                "SUM(re.nbr_place_reservee) AS total_reserved_places " +
                "FROM excursion e " +
                "LEFT JOIN reservationexcursion re ON e.id_excursion = re.excursion_id " + // Table reservationexcursion
                "LEFT JOIN reservation r ON r.id_reservation = re.id_reservation " + // Table reservation pour accéder au statut
                "WHERE r.statut = 'CONFIRMEE' " +  // Filtrage des réservations confirmées
                "GROUP BY e.id_excursion";

        List<ExcursionStat> statsList = new ArrayList<>();

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int excursionId = resultSet.getInt("id_excursion");
                String excursionName = resultSet.getString("excursion_name");
                int reservationsCount = resultSet.getInt("reservations_count");
                int totalReservedPlaces = resultSet.getInt("total_reserved_places");

                // Créer une instance d'ExcursionStat avec le nom de l'excursion, et ajouter à la liste
                ExcursionStat stat = new ExcursionStat(
                        excursionName,  // Nom de l'excursion
                        excursionId,    // Id de l'excursion
                        reservationsCount,  // Nombre de réservations confirmées
                        totalReservedPlaces, // Nombre total de places réservées
                        new Timestamp(System.currentTimeMillis()) // Date actuelle comme date de création
                );

                statsList.add(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statsList;
    }


    // ✅ Trouver un guide par ID
    public Guide trouverGuideParId(int idGuide) throws SQLException {
        String sql = "SELECT * FROM guide WHERE id_guide = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, idGuide);

        ResultSet rs = statement.executeQuery();

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
        return null; // Retourner null si le guide n'est pas trouvé
    }

    public void ajouter(Excursion excursion) {
        String reqGuide = "SELECT id_guide FROM guide WHERE nom = ? AND prenom = ?";
        String reqExcursion = "INSERT INTO excursion (nom, lieu, prix, date_depart, date_retour, description, image, id_guide, nombre_max_participants, ville_depart, disponibilite) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement psGuide = connection.prepareStatement(reqGuide)) {
            psGuide.setString(1, excursion.getGuide().getNom());
            psGuide.setString(2, excursion.getGuide().getPrenom());
            try (ResultSet rsGuide = psGuide.executeQuery()) {
                if (!rsGuide.next()) return;
                int idGuide = rsGuide.getInt("id_guide");
                excursion.getGuide().setIdGuide(idGuide);

                try (PreparedStatement ps = connection.prepareStatement(reqExcursion)) {
                    ps.setString(1, excursion.getNom());
                    ps.setString(2, excursion.getLieu());
                    ps.setDouble(3, excursion.getPrix());
                    ps.setTimestamp(4, Timestamp.valueOf(excursion.getDateDepart().atStartOfDay()));
                    ps.setTimestamp(5, Timestamp.valueOf(excursion.getDateRetour().atStartOfDay()));
                    ps.setString(6, excursion.getDescription());
                    ps.setString(7, excursion.getImage());
                    ps.setInt(8, idGuide);
                    ps.setInt(9, excursion.getNbMaxParticipants());
                    ps.setString(10, excursion.getVilleDepart());
                    ps.setBoolean(11, excursion.isDisponibilite());

                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ✅ Modifier une excursion

    @Override
    public boolean modifier(Excursion excursion) throws SQLException {
        String req = "UPDATE excursion SET nom=?, lieu=?, prix=?, date_depart=?, date_retour=?, description=?, ville_depart=?, disponibilite=? WHERE id_excursion=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, excursion.getNom());
        ps.setString(2, excursion.getLieu());
        ps.setDouble(3, excursion.getPrix());
        ps.setDate(4, java.sql.Date.valueOf(excursion.getDateDepart()));
        ps.setDate(5, java.sql.Date.valueOf(excursion.getDateRetour()));
        ps.setString(6, excursion.getDescription());
        ps.setString(7, excursion.getVilleDepart());
        ps.setBoolean(8, excursion.isDisponibilite());
        ps.setInt(9, excursion.getIdExcursion());

        return ps.executeUpdate() > 0;
    }


    // ✅ Supprimer une excursion
    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM excursion WHERE id_excursion = ?";
        PreparedStatement statement = connection.prepareStatement(req);
        statement.setInt(1, id);

        int rowsDeleted = statement.executeUpdate();
        if (rowsDeleted > 0) {
            System.out.println("Excursion supprimée avec succès.");
        } else {
            System.out.println("Aucune excursion trouvée avec cet ID.");
        }
    }

    // ✅ Lire toutes les excursions
    @Override
    public List<Excursion> lire() throws SQLException {
        List<Excursion> excursions = new ArrayList<>();
        String req = "SELECT e.*, e.disponibilite, g.nom AS nom_guide, g.prenom AS prenom_guide, g.langues, " +
                "g.numero_tel AS telephone, g.email, g.image AS image_guide " +
                "FROM excursion e " +
                "JOIN guide g ON e.id_guide = g.id_guide";


        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(req);

        while (rs.next()) {
            Excursion excursion = new Excursion();
            excursion.setIdExcursion(rs.getInt("id_excursion"));
            excursion.setNom(rs.getString("nom"));
            excursion.setLieu(rs.getString("lieu"));
            excursion.setPrix(rs.getDouble("prix"));
            excursion.setDateDepart(rs.getTimestamp("date_depart").toLocalDateTime().toLocalDate());
            excursion.setDateRetour(rs.getTimestamp("date_retour").toLocalDateTime().toLocalDate());
            excursion.setDisponibilite(rs.getBoolean("disponibilite"));

            excursion.setDescription(rs.getString("description"));
            excursion.setImage(rs.getString("image"));  // Image de l'excursion
            excursion.setNbPlace(rs.getInt("nombre_max_participants"));
            excursion.setVilleDepart(rs.getString("ville_depart"));

            Guide guide = new Guide();
            guide.setIdGuide(rs.getInt("id_guide"));
            guide.setNom(rs.getString("nom_guide"));
            guide.setPrenom(rs.getString("prenom_guide"));
            guide.setLangue(rs.getString("langues"));
            guide.setTelephone(rs.getString("telephone"));  // ✅ utilise l'alias
            guide.setEmail(rs.getString("email"));
            guide.setImage(rs.getString("image_guide"));  // ✅ Utilisation du bon alias pour éviter la confusion

            excursion.setGuide(guide);

            excursions.add(excursion);
        }
        return excursions;
    }


    // ✅ Méthode pour obtenir toutes les excursions
    public List<Excursion> getAllExcursions() throws SQLException {
        List<Excursion> excursions = new ArrayList<>();
        String req = "SELECT e.*, g.nom AS nom_guide, g.prenom AS prenom_guide " +
                "FROM excursion e " +
                "LEFT JOIN guide g ON e.id_guide = g.id_guide";

        try (PreparedStatement statement = connection.prepareStatement(req);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Excursion excursion = new Excursion();
                excursion.setIdExcursion(rs.getInt("id_excursion"));
                excursion.setNom(rs.getString("nom"));
                excursion.setLieu(rs.getString("lieu"));
                excursion.setPrix(rs.getDouble("prix"));
                excursion.setDateDepart(rs.getDate("date_depart").toLocalDate());
                excursion.setDateRetour(rs.getDate("date_retour").toLocalDate());
                excursion.setDisponibilite(rs.getBoolean("disponibilite"));

                excursion.setDescription(rs.getString("description"));
                excursion.setImage(rs.getString("image"));
                excursion.setNbPlace(rs.getInt("nombre_max_participants"));
                excursion.setVilleDepart(rs.getString("ville_depart"));

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
    public Excursion getInfo(int idExcursion) throws SQLException {
        String sql = "SELECT e.*, g.id_guide, g.nom AS guide_nom, g.prenom AS guide_prenom, g.image AS guide_image, g.langues, g.numeroTel, g.email " +
                "FROM excursion e " +
                "LEFT JOIN guide g ON e.id_guide = g.id_guide " +
                "WHERE e.id_excursion = ?";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, idExcursion);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            Guide guide = new Guide(
                    rs.getInt("id_guide"),
                    rs.getString("guide_nom"),
                    rs.getString("guide_prenom"),
                    rs.getString("guide_image"),
                    rs.getString("langues"),
                    rs.getString("numero_tel"),
                    rs.getString("email")
            );

            return new Excursion(
                    rs.getInt("id_excursion"),
                    rs.getString("nom"),
                    rs.getString("lieu"),
                    rs.getDouble("prix"),
                    rs.getDate("date_depart").toLocalDate(),
                    rs.getDate("date_retour").toLocalDate(),
                    rs.getString("description"),
                    rs.getString("image"),
                    guide,
                    rs.getInt("nombre_max_participants"),
                    rs.getString("ville_depart")
            );
        }

        return null; // Retourner null si aucune excursion n'est trouvée
    }

    // ✅ Réutilisation pour lister toutes les excursions
    public List<Excursion> lister() throws SQLException {
        return getAllExcursions();
    }
}
