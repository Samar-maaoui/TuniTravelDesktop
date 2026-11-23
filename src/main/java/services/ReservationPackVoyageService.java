package services;


import com.mailersend.sdk.exceptions.MailerSendException;
import entities.*;
import utils.MyDatabase;
import utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationPackVoyageService implements IReservationPackVoyage {

    private static Connection connection;

    public ReservationPackVoyageService() {
        connection = MyDatabase.getInstance().getConnection();
    }



    public void ajouterReservation(ReservationPackVoyage reservation) throws SQLException {
        // Récupérer l'utilisateur connecté via SessionManager
        Utilisateur utilisateurConnecte = SessionManager.getInstance().getUtilisateur();
        if (utilisateurConnecte == null) {
            System.err.println("Erreur : Aucun utilisateur connecté.");
            throw new SQLException("Aucun utilisateur connecté.");
        }

        // Vérification du nombre de places restantes dans le pack de voyage
        int placesRestantes = reservation.getPackVoyage().getNombrePlacesRestants();
        System.out.println("Places restantes pour le pack " + reservation.getPackVoyage().getId() + ": " + placesRestantes);

        if (reservation.getPlacesDemandees() > placesRestantes) {
            System.err.println("Erreur : Le nombre de places demandées dépasse les places restantes disponibles.");
            throw new SQLException("Le nombre de places demandées dépasse les places restantes disponibles.");
        }

        // Récupérer l'ID du post associé au pack de voyage
        int postId = getPostIdByPackId(reservation.getPackVoyage().getId());
        System.out.println("ID du post récupéré pour le pack ID " + reservation.getPackVoyage().getId() + ": " + postId);

        // Préparer l'insertion dans la table reservationpackvoyage
        String sql = """
        INSERT INTO reservationpackvoyage 
        (utilisateur_id, date_reservation, statut, statut_paiement, pack_id, places_demandees, montant_total, post_id) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, utilisateurConnecte.getIdUtilisateur());
            stmt.setTimestamp(2, reservation.getDateReservation());  // Utilisation de setTimestamp pour l'heure exacte
            stmt.setString(3, reservation.getStatut().name());
            stmt.setString(4, reservation.getStatutPaiement().name());
            stmt.setInt(5, reservation.getPackVoyage().getId());
            stmt.setInt(6, reservation.getPlacesDemandees());
            stmt.setDouble(7, reservation.getMontantTotal());
            stmt.setInt(8, postId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reservation.setIdReservation(generatedKeys.getInt(1));
                    System.out.println("Réservation ajoutée avec succès pour le pack ID: " + reservation.getPackVoyage().getId());
                }

                // Calcul du nombre de places restantes
                int placesRestantesMisesAJour = placesRestantes - reservation.getPlacesDemandees();
                System.out.println("Nouvelles places restantes après la réservation : " + placesRestantesMisesAJour);

                // Mettre à jour le nombre de places restantes dans PackVoyage
                if (placesRestantesMisesAJour >= 0) {
                    String updatePackVoyageSQL = "UPDATE packvoyage SET nombre_places_restants = ? WHERE id_pack = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updatePackVoyageSQL)) {
                        updateStmt.setInt(1, placesRestantesMisesAJour);
                        updateStmt.setInt(2, reservation.getPackVoyage().getId());
                        updateStmt.executeUpdate();
                        System.out.println("Places restantes mises à jour dans PackVoyage.");
                    }
                } else {
                    System.out.println("Erreur: Le nombre de places restantes ne peut pas être inférieur à zéro.");
                }

                // Récupérer les détails du pack de voyage, vol, excursion, hôtel
                PackVoyage packVoyage = PackVoyageService.getInstance().trouverPackParId(reservation.getPackVoyage().getId());
                Hotel hotel = packVoyage.getHotel();
                vol vol = packVoyage.getVol();
                Excursion excursion = packVoyage.getExcursion();

                // Personnalisation de l'email avec les informations de la réservation
                String email = utilisateurConnecte.getEmail(); // Récupérer l'email de l'utilisateur
                String subject = "Confirmation de votre réservation";
                String body = "Bonjour " + utilisateurConnecte.getNom() + ",\n\n" +
                        "Votre réservation pour le pack de voyage \"" + packVoyage.getNomPack() + "\" a été confirmée.\n\n" +
                        "Détails de la réservation :\n" +
                        "Pack de voyage : " + packVoyage.getNomPack() + "\n" +
                        "Nombre de places réservées : " + reservation.getPlacesDemandees() + "\n" +
                        "Montant total : " + reservation.getMontantTotal() + "€\n" +
                        "Date de réservation : " + reservation.getDateReservation() + "\n\n" +

                        // Ajout des détails de l'excursion, du vol et de l'hôtel
                        "Détails supplémentaires :\n" +
                        "Excursion : " + (excursion != null ? excursion.getNom() : "Aucune excursion sélectionnée") + "\n" +
                        "Vol : " + (vol != null ? vol.getnumero_vol() : "Aucun vol sélectionné") + "\n" +
                        "Hôtel : " + (hotel != null ? hotel.getNom() : "Aucun hôtel sélectionné") + "\n\n" +

                        "Merci de votre confiance.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe de réservation.";

                // Envoi de l'email via MailerSend
                MailerSendService.sendEmail(email, subject, body);
                System.out.println("Email de confirmation envoyé à : " + email);
            }
        }
    }






    public int getPostIdByPackId(int packId) throws SQLException {
        String sql = "SELECT id_post FROM post WHERE pack_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, packId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_post"); // Récupérer l'ID du post associé au pack
            } else {
                throw new SQLException("Aucun post trouvé pour le pack avec ID " + packId);
            }
        }
    }




    @Override
    public ReservationPackVoyage trouverReservationParId(int reservationId) throws SQLException {
        String sql = "SELECT * FROM reservationpackvoyage WHERE id_reservation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idReservation = rs.getInt("id_reservation");
                int utilisateurId = rs.getInt("utilisateur_id");
                Date dateReservation = rs.getDate("date_reservation");
                StatutReservation statut = StatutReservation.valueOf(rs.getString("statut"));
                StatutPaiement statutPaiement = StatutPaiement.valueOf(rs.getString("statut_paiement"));
                int packId = rs.getInt("pack_id");
                int placesDemandees = rs.getInt("places_demandees");
                double montantTotal = rs.getDouble("montant_total");

                // On crée l'objet ReservationPackVoyage avec les données récupérées
                ReservationPackVoyage reservation = new ReservationPackVoyage(
                        idReservation,
                        new Utilisateur(utilisateurId),  // Vous pouvez créer un objet Utilisateur ici si vous voulez l'utiliser dans la réservation
                        dateReservation,
                        statut,
                        statutPaiement,
                        new PackVoyage(packId),  // Assurez-vous que PackVoyage est correctement initialisé
                        null,  // Le Post peut être récupéré ou laissé nul si non nécessaire
                        placesDemandees,
                        montantTotal
                );
                return reservation;
            }
        }
        return null;  // Si aucune réservation n'est trouvée avec l'ID donné
    }

    @Override
    public void modifierReservation(ReservationPackVoyage reservation) throws SQLException {
        String sql = "UPDATE reservationpackvoyage SET statut = ?, statut_paiement = ?, places_demandees = ?, montant_total = ? WHERE id_reservation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reservation.getStatut().name());  // Le statut de la réservation
            stmt.setString(2, reservation.getStatutPaiement().name());  // Le statut du paiement
            stmt.setInt(3, reservation.getPlacesDemandees());  // Nombre de places demandées
            stmt.setDouble(4, reservation.getMontantTotal());  // Montant total de la réservation
            stmt.setInt(5, reservation.getIdReservation());  // ID de la réservation à modifier

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune réservation modifiée.");
            }

            // Réduction des places restantes dans le pack de voyage si nécessaire
            if (reservation.getPlacesDemandees() > 0) {
                PackVoyageService packVoyageService = new PackVoyageService();
                packVoyageService.mettreAJourPlacesRestantes(reservation.getPackVoyage().getId(), reservation.getPlacesDemandees());
            }

            // Mise à jour du statut du Post lié
            PostService postService = PostService.getInstance();
            Post post = postService.trouverParId(reservation.getPackVoyage().getId());
            if (post != null) {
                // Mettre à jour le statut du Post selon les places restantes
                if (post.getPackVoyage().getNombrePlacesRestants() == 0) {
                    post.setStatut(StatutPost.EXPIRÉ);
                } else {
                    post.setStatut(StatutPost.DISPONIBLE);
                }
                postService.modifierPost(post);
            }
        }
    }

    @Override
    public void supprimerReservation(int reservationId) throws SQLException {
        String sql = "DELETE FROM reservationpackvoyage WHERE id_reservation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);  // ID de la réservation à supprimer

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune réservation supprimée.");
            }

            // Logique pour restaurer les places restantes dans le pack de voyage si nécessaire
            // Vous pouvez ajouter ici la logique pour restaurer le nombre de places restantes
            // si la réservation supprimée affecte le nombre de places disponibles

            System.out.println("Réservation supprimée avec succès.");
        }
    }


    // Lister toutes les réservations
    public List<ReservationPackVoyage> listerReservations(int utilisateurId) throws SQLException {
        List<ReservationPackVoyage> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservationpackvoyage WHERE utilisateur_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, utilisateurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idReservation = rs.getInt("id_reservation");
                    Timestamp dateReservation = rs.getTimestamp("date_reservation");
                    String statut = rs.getString("statut");
                    String statutPaiement = rs.getString("statut_paiement");
                    int packId = rs.getInt("pack_id");
                    int placesDemandees = rs.getInt("places_demandees");
                    double montantTotal = rs.getDouble("montant_total");

                    Utilisateur utilisateur = getUtilisateurById(utilisateurId);
                    PackVoyage packVoyage = getPackVoyageById(packId);

                    ReservationPackVoyage reservation = new ReservationPackVoyage(
                            idReservation, utilisateur, dateReservation,
                            StatutReservation.valueOf(statut), StatutPaiement.valueOf(statutPaiement),
                            packVoyage, null, placesDemandees, montantTotal
                    );

                    reservations.add(reservation);
                }
            }
        }

        return reservations;
    }


    private PackVoyage getPackVoyageById(int packId) throws SQLException {
        String sql = "SELECT * FROM packvoyage WHERE id_pack = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, packId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new PackVoyage(
                        rs.getInt("id_pack"),
                        rs.getString("nomPack"),
                        rs.getDouble("prix_total"),
                        rs.getInt("nombre_places_restants")
                );
            } else {
                throw new SQLException("Aucun pack trouvé avec l'ID " + packId);
            }
        }
    }


    private Utilisateur getUtilisateurById(int utilisateurId) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, utilisateurId);
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


    // Lister les réservations d'un pack de voyage par pack_id
    @Override
    public List<ReservationPackVoyage> listerReservationsParPackId(int packId) throws SQLException {
        List<ReservationPackVoyage> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservationpackvoyage WHERE pack_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, packId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idReservation = rs.getInt("id_reservation");
                int utilisateurId = rs.getInt("utilisateur_id");
                Timestamp dateReservation = rs.getTimestamp("date_reservation");  // Utiliser getTimestamp pour récupérer la date et l'heure

                String statut = rs.getString("statut");
                String statutPaiement = rs.getString("statut_paiement");
                int placesDemandees = rs.getInt("places_demandees");
                double montantTotal = rs.getDouble("montant_total");

                ReservationPackVoyage reservation = new ReservationPackVoyage(idReservation, utilisateurId, dateReservation, StatutReservation.valueOf(statut), StatutPaiement.valueOf(statutPaiement), new PackVoyage(packId), null, placesDemandees, montantTotal);
                reservations.add(reservation);
            }
        }
        return reservations;
    }
    public Post trouverPostAvecPackVoyageParId(int postId) throws SQLException {
        String sql = """
        SELECT p.id_post, p.titre, p.description, p.date_publication, p.date_expiration, p.statut, p.pack_id, p.image_url,
               pv.id_pack, pv.nomPack, pv.prix_total, pv.nombre_places_restants
        FROM Post p
        INNER JOIN PackVoyage pv ON p.pack_id = pv.id_pack
        WHERE p.id_post = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, postId); // Remplacer le ? par l'ID du Post
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Créer un PackVoyage avec les informations récupérées
                PackVoyage packVoyage = new PackVoyage(
                        rs.getInt("id_pack"), // ID du Pack
                        rs.getString("nomPack"), // Nom du Pack
                        rs.getDouble("prix_total"), // Prix total
                        rs.getInt("nombre_places_restants") // Nombre de places restantes
                );

                // Créer le Post avec les informations récupérées et associer le PackVoyage
                Post post = new Post(
                        rs.getInt("id_post"), // ID du Post
                        rs.getString("titre"), // Titre du Post
                        rs.getString("description"), // Description du Post
                        rs.getDate("date_expiration"), // Date d'expiration du Post
                        packVoyage, // Associer le PackVoyage récupéré
                        StatutPost.valueOf(rs.getString("statut")), // Statut du Post
                        rs.getString("image_url") // URL de l'image du Post
                );

                return post;
            } else {
                throw new SQLException("Aucun post trouvé avec l'ID " + postId);
            }
        }
    }
    public void modifierPost(Post post) throws SQLException {
        String sql = "UPDATE post SET statut = ? WHERE id_post = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, post.getStatut().name()); // Le statut du post (DISPONIBLE ou EXPIRÉ)
            stmt.setInt(2, post.getIdPost()); // ID du post à mettre à jour
            stmt.executeUpdate();
        }
    }
    public void mettreAJourStatutPaiementEtConfirmation(int reservationId) throws SQLException {
        // Mettre à jour le statut de la réservation à CONFIRMEE et le statut de paiement à PAYE
        String sql = "UPDATE reservationpackvoyage SET statut = ?, statut_paiement = ? WHERE id_reservation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, StatutReservation.CONFIRMEE.name());  // Mettre le statut à CONFIRMEE
            stmt.setString(2, StatutPaiement.PAYE.name());  // Mettre le statut du paiement à PAYE
            stmt.setInt(3, reservationId);  // ID de la réservation à mettre à jour

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Statut de la réservation et du paiement mis à jour avec succès.");
            } else {
                System.out.println("Aucune réservation trouvée avec l'ID : " + reservationId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Erreur lors de la mise à jour des statuts de la réservation et du paiement.");
        }
    }



}


