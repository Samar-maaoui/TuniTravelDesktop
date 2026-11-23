package services;

import entities.*;
import utils.MyDatabase;
import utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackVoyageService implements IPackVoyage<PackVoyage> {
    private static Connection connection;
    private static PackVoyageService instance;
    public PackVoyageService() {
        connection = MyDatabase.getInstance().getConnection();
    }
    public static synchronized PackVoyageService getInstance() {
        if (instance == null) {
            instance = new PackVoyageService();  // Créer une nouvelle instance si elle n'existe pas déjà
        }
        return instance;  // Retourner l'instance existante
    }

    @Override
    public void ajouterPack(PackVoyage packVoyage) throws SQLException {


        String sql = "INSERT INTO packvoyage (nomPack, details, prix_total, hotel_id, vol_id, excursion_id, nombre_places_restants, admin_id, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, packVoyage.getNomPack());
            stmt.setString(2, packVoyage.getDetails());
            stmt.setDouble(3, packVoyage.getPrixTotal());
            stmt.setInt(4, packVoyage.getHotel().getId_hotel());
            stmt.setInt(5, packVoyage.getVol().getId_vol());
            stmt.setInt(6, packVoyage.getExcursion().getIdExcursion());
            stmt.setInt(7, packVoyage.getNombrePlacesRestants());

            // Vérification si l'utilisateur est un administrateur
            Utilisateur utilisateurConnecte = SessionManager.getInstance().getUtilisateur();
            if (utilisateurConnecte == null || utilisateurConnecte.getTypeUtilisateur() != TypeUtilisateur.ADMIN) {
                throw new SQLException("Seul un administrateur peut ajouter un pack de voyage.");
            }

            stmt.setInt(8, utilisateurConnecte.getIdUtilisateur());
            stmt.setString(9, packVoyage.getImageUrl());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    packVoyage.setId(generatedKeys.getInt(1));  // Récupérer l'ID généré
                }
                System.out.println("Pack Voyage ajouté avec succès : " + packVoyage.getNomPack());
            } else {
                System.out.println("Échec de l'ajout du Pack Voyage.");
            }
        }
    }

    @Override
    public List<PackVoyage> listerPacks() throws SQLException {
        List<PackVoyage> packVoyages = new ArrayList<>();
        String sql = "SELECT * FROM packvoyage";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id_pack");
                String nom = rs.getString("nomPack");
                String description = rs.getString("details");
                double prix = rs.getDouble("prix_total");
                int hotelId = rs.getInt("hotel_id");
                int volId = rs.getInt("vol_id");
                int excursionId = rs.getInt("excursion_id");
                int nombrePlacesRestants = rs.getInt("nombre_places_restants");
                String imageUrl = rs.getString("image_url");
                int adminId = rs.getInt("admin_id");

                // Récupérer les objets associés aux ID directement par SQL sans appel de services externes
                Hotel hotel = getHotelById(hotelId);
                vol vol = getVolById(volId);
                Excursion excursion = getExcursionById(excursionId);
                Utilisateur admin = getUtilisateurById(adminId);

                // Création de l'objet PackVoyage
                PackVoyage pack = new PackVoyage(id, nom, description, prix, hotel, vol, excursion, nombrePlacesRestants, admin, imageUrl);
                packVoyages.add(pack);
            }
        }
        return packVoyages;
    }

    @Override
    public PackVoyage trouverPackParNom(String nom) throws SQLException {
        PackVoyage packVoyage = null;
        String query = "SELECT * FROM packvoyage WHERE nomPack = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nom);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id_pack");
                String description = resultSet.getString("details");
                double prix = resultSet.getDouble("prix_total");
                int hotelId = resultSet.getInt("hotel_id");
                int volId = resultSet.getInt("vol_id");
                int excursionId = resultSet.getInt("excursion_id");
                int nombrePlacesRestants = resultSet.getInt("nombre_places_restants");
                String imageUrl = resultSet.getString("image_url");
                int adminId = resultSet.getInt("admin_id");

                Hotel hotel = getHotelById(hotelId);
                vol vol = getVolById(volId);
                Excursion excursion = getExcursionById(excursionId);
                Utilisateur admin = getUtilisateurById(adminId);

                packVoyage = new PackVoyage(id, nom, description, prix, hotel, vol, excursion, nombrePlacesRestants, admin, imageUrl);
            }
        }
        return packVoyage;
    }

    public Hotel getHotelById(int hotelId) throws SQLException {
        System.out.println("Récupération de l'hôtel avec l'ID : " + hotelId);
        String sql = "SELECT * FROM hotel WHERE id_hotel = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, hotelId);  // Assurez-vous que l'ID est bien passé
            ResultSet rs = stmt.executeQuery();

            // Vérification si l'hôtel est récupéré
            if (rs.next()) {
                Hotel hotel = new Hotel(
                        rs.getInt("id_hotel"),
                        rs.getString("nom"),
                        rs.getDouble("prix_nuit"),  // Récupération d'autres champs si nécessaires
                        rs.getString("adresse"),
                        rs.getString("description"),
                        rs.getString("tel"),
                        rs.getString("email_contact"),
                        rs.getString("image_hotel")
                );
                System.out.println("Hôtel récupéré : " + hotel.getNom());
                return hotel;
            } else {
                System.out.println("Aucun hôtel trouvé avec l'ID : " + hotelId);
            }
        }
        return null;  // Si aucun hôtel n'est trouvé
    }




    private vol getVolById(int volId) throws SQLException {
        System.out.println("Récupération du vol avec l'ID : " + volId);

        // Vérifier si l'ID de vol est valide
        if (volId <= 0) {
            System.out.println("ID de vol invalide : " + volId);
            return null;
        }

        String sql = "SELECT * FROM vol WHERE id_vol = ?";  // Requête pour récupérer un vol par son ID

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, volId);  // Passer l'ID du vol dans la requête
            ResultSet rs = stmt.executeQuery();

            // Vérifier si des résultats sont retournés
            if (rs.next()) {
                // Affichage des détails du vol pour débogage
                System.out.println("Vol trouvé : ");
                System.out.println("ID Vol : " + rs.getInt("id_vol"));
                System.out.println("Numéro de vol : " + rs.getString("numero_vol"));
                System.out.println("Compagnie : " + rs.getString("compagnie"));
                System.out.println("Prix : " + rs.getDouble("prix"));
                System.out.println("Aéroport départ : " + rs.getString("aero_depart"));
                System.out.println("Aéroport arrivée : " + rs.getString("aero_arrivee"));
                System.out.println("Date départ : " + rs.getDate("date_depart"));
                System.out.println("Date arrivée : " + rs.getDate("date_arrivee"));

                // Retourner un objet vol avec les données récupérées de la base de données
                return new vol(
                        rs.getInt("id_vol"),  // ID du vol
                        rs.getInt("disponible"),  // Disponibilité du vol
                        rs.getString("numero_vol"),  // Numéro du vol
                        rs.getString("compagnie"),  // Compagnie aérienne
                        rs.getString("aero_depart"),  // Aéroport de départ
                        rs.getString("aero_arrivee"),  // Aéroport d'arrivée
                        rs.getDouble("prix"),  // Prix du vol
                        rs.getDate("date_depart"),  // Date de départ
                        rs.getDate("date_arrivee")  // Date d'arrivée
                );
            } else {
                // Si aucun vol n'est trouvé pour l'ID donné
                System.out.println("Aucun vol trouvé avec l'ID : " + volId);
            }
        }
        return null;  // Retourne null si aucun vol n'est trouvé
    }





    private Excursion getExcursionById(int excursionId) throws SQLException {
        String sql = "SELECT * FROM excursion WHERE id_excursion = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, excursionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Excursion(
                        rs.getInt("id_excursion"), // ID de l'excursion
                        rs.getString("nom"),        // Nom de l'excursion
                        rs.getString("lieu"),       // Lieu de l'excursion
                        rs.getDouble("prix"),       // Prix de l'excursion
                        rs.getDate("date_depart").toLocalDate(), // Date de départ (conversion de java.sql.Date en LocalDate)
                        rs.getDate("date_retour").toLocalDate(), // Date de retour (conversion de java.sql.Date en LocalDate)
                        rs.getString("description"), // Description de l'excursion
                        rs.getString("image"),       // Image associée
                        rs.getInt("nombre_max_participants"), // Nombre maximum de participants
                        rs.getString("ville_depart")  // New field: ville_depart
                );
            }
        }
        return null;
    }



    private Utilisateur getUtilisateurById(int adminId) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getInt("cin"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getDate("date_naissance").toLocalDate(),
                        TypeUtilisateur.valueOf(rs.getString("type_Utilisateur").toUpperCase()),
                        rs.getBoolean("is_verified")
                );
            }
        }
        return null;
    }

    private boolean estPackReserve(int packId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservationpackvoyage WHERE pack_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, packId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // Si le pack a été réservé
            }
        }
        return false;  // Si le pack n'a pas été réservé
    }

    @Override
    public void modifierPack(PackVoyage packVoyage) throws SQLException {
        // Vérification si le pack a été réservé par un client
        if (!estPackReserve(packVoyage.getId())) {
            // Si le pack n'a pas été réservé, on bloque la modification
            throw new SQLException("Le pack de voyage n'a pas été réservé, modification impossible.");
        }

        // Récupérer l'utilisateur actuellement connecté (client ou administrateur)
        Utilisateur utilisateurConnecte = SessionManager.getInstance().getUtilisateur();
        if (utilisateurConnecte == null || (utilisateurConnecte.getTypeUtilisateur() != TypeUtilisateur.ADMIN && utilisateurConnecte.getTypeUtilisateur() != TypeUtilisateur.NORMAL)) {
            // Vérifier si l'utilisateur est soit un admin, soit un client pour modifier
            throw new SQLException("Seul un administrateur ou un client ayant réservé ce pack peut le modifier.");
        }

        // Requête pour mettre à jour les informations du pack de voyage
        String sql = "UPDATE packvoyage SET nomPack=?, details=?, prix_total=?, hotel_id=?, vol_id=?, excursion_id=?, nombre_places_restants=?, image_url=? WHERE id_pack=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Affecter les valeurs dans la requête préparée
            stmt.setString(1, packVoyage.getNomPack());
            stmt.setString(2, packVoyage.getDetails());
            stmt.setDouble(3, packVoyage.getPrixTotal());
            stmt.setInt(4, packVoyage.getHotel().getId_hotel());  // Récupérer l'ID de l'hôtel
            stmt.setInt(5, packVoyage.getVol().getId_vol());  // Récupérer l'ID du vol
            stmt.setInt(6, packVoyage.getExcursion().getIdExcursion());  // Récupérer l'ID de l'excursion
            stmt.setInt(7, packVoyage.getNombrePlacesRestants());
            stmt.setString(8, packVoyage.getImageUrl());  // URL de l'image du pack
            stmt.setInt(9, packVoyage.getId());  // ID du pack à modifier

            // Exécuter la mise à jour
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Pack Voyage mis à jour avec succès : " + packVoyage.getNomPack());
            } else {
                System.out.println("Aucun Pack Voyage trouvé avec ID : " + packVoyage.getId());
            }
        }
    }




    @Override
    public void supprimerPack(PackVoyage packVoyage) throws SQLException {
        String sql = "DELETE FROM packvoyage WHERE id_pack = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, packVoyage.getId());
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Pack Voyage supprimé avec succès (ID: " + packVoyage.getId() + ")");
            } else {
                System.out.println("Aucun Pack Voyage trouvé avec ID : " + packVoyage.getId());
            }
        }
    }

    @Override
    public PackVoyage trouverPackParId(int id) throws SQLException {
        PackVoyage packVoyage = null;
        String query = "SELECT * FROM packvoyage WHERE id_pack = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String nom = resultSet.getString("nomPack");
                String description = resultSet.getString("details");
                double prix = resultSet.getDouble("prix_total");
                int hotelId = resultSet.getInt("hotel_id");
                int volId = resultSet.getInt("vol_id");
                int excursionId = resultSet.getInt("excursion_id");
                int nombrePlacesRestants = resultSet.getInt("nombre_places_restants");
                String imageUrl = resultSet.getString("image_url");
                int adminId = resultSet.getInt("admin_id");

                Hotel hotel = getHotelById(hotelId);
                vol vol = getVolById(volId);
                Excursion excursion = getExcursionById(excursionId);
                Utilisateur admin = getUtilisateurById(adminId);

                packVoyage = new PackVoyage(id, nom, description, prix, hotel, vol, excursion, nombrePlacesRestants, admin, imageUrl);
            }
        }
        return packVoyage;
    }

    @Override
    public boolean placesDisponibles(int packId) throws SQLException {
        String query = "SELECT capacite - (SELECT COUNT(*) FROM reservations WHERE pack_voyage_id = ?) AS places_disponibles FROM pack_voyages WHERE id_pack_voyage = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, packId);
            statement.setInt(2, packId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int placesDisponibles = resultSet.getInt("places_disponibles");
                return placesDisponibles > 0;
            }
        }
        return false;
    }

    @Override
    public void verifierStatutPack() throws SQLException {
        String sql = "UPDATE pack_voyages SET statut = 'Complet' WHERE id_pack_voyage IN (SELECT id_pack_voyage FROM pack_voyages WHERE capacite = (SELECT COUNT(*) FROM reservations WHERE pack_voyage_id = id_pack_voyage))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("Pack status updated to 'Complet' for full packs.");
        }
    }
    public List<vol> getAllVols() throws SQLException {
        List<vol> vols = new ArrayList<>();
        String sql = "SELECT * FROM vol";  // Récupère tous les vols

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vol v = new vol(
                        rs.getInt("id_vol"),
                        rs.getInt("disponible"),
                        rs.getString("numero_vol"),
                        rs.getString("compagnie"),
                        rs.getString("aero_depart"),
                        rs.getString("aero_arrivee"),
                        rs.getDouble("prix"),
                        rs.getDate("date_depart"),
                        rs.getDate("date_arrivee")
                );
                vols.add(v);  // Ajouter le vol à la liste
            }
        }
        return vols;
    }
    public List<Excursion> listerExcursions() throws SQLException {
        List<Excursion> excursions = new ArrayList<>();
        String sql = "SELECT * FROM excursion";  // Récupère toutes les excursions

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Créer un objet Excursion avec les données récupérées
                Excursion excursion = new Excursion(
                        rs.getInt("id_excursion"),
                        rs.getString("nom"),
                        rs.getString("lieu"),
                        rs.getDouble("prix"),
                        rs.getDate("date_depart").toLocalDate(),
                        rs.getDate("date_retour").toLocalDate(),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getInt("nombre_max_participants")
                );
                excursions.add(excursion);  // Ajouter l'excursion à la liste
            }
        }
        return excursions;
    }
    public List<Hotel> listerHotels() throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotel";  // Récupère tous les hôtels

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Créer un objet Hotel avec les données récupérées
                Hotel hotel = new Hotel(
                        rs.getInt("id_hotel"),
                        rs.getString("nom"),
                        rs.getDouble("prix_nuit"),
                        rs.getString("adresse"),
                        rs.getString("description"),
                        rs.getString("tel"),
                        rs.getString("email_contact"),
                        rs.getString("image_hotel")
                );
                hotels.add(hotel);  // Ajouter l'hôtel à la liste
            }
        }
        return hotels;
    }
    public void mettreAJourPlacesRestantes(int packVoyageId, int placesDemandees) {
        String reqUpdatePlacesRestantes = "UPDATE packvoyage SET nombre_places_restants = nombre_places_restants - ? WHERE id_pack = ?";

        try (PreparedStatement stmtUpdate = connection.prepareStatement(reqUpdatePlacesRestantes)) {
            stmtUpdate.setInt(1, placesDemandees);
            stmtUpdate.setInt(2, packVoyageId);
            int rowsUpdated = stmtUpdate.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Le nombre de places restantes a été mis à jour.");
            } else {
                System.out.println("Aucune mise à jour effectuée pour le nombre de places.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des places restantes pour le pack voyage : " + e.getMessage());
        }
    }




}
