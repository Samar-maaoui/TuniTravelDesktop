package services;

import entities.*;
import utils.ImageUtils;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService implements IPost<Post> {
    private static PostService instance;
    private final Connection connection;

    // Constructeur privé pour le Singleton
    private PostService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // Méthode d'accès unique (Singleton)
    public static synchronized PostService getInstance() {
        if (instance == null) {
            instance = new PostService();
        }
        return instance;
    }

    // Ajouter un post
    @Override
    public void ajouterPost(Post post) {
        String sql = "INSERT INTO Post (titre, description, date_publication, date_expiration, statut, pack_id, image_url) VALUES (?, ?, NOW(), ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, post.getTitre());
            stmt.setString(2, post.getDescription());
            stmt.setDate(3, new java.sql.Date(post.getDateExpiration().getTime()));
            stmt.setString(4, post.getStatut().name());
            stmt.setInt(5, post.getPackVoyage().getId());

            // Gestion de l’image
            String imageUrl = ImageUtils.enregistrerImage(post.getImageUrl());
            stmt.setString(6, imageUrl);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de l'ajout du Post", e);
        }
    }

    // Lister les posts avec les détails du PackVoyage
    @Override
    public List<Post> listerPosts() {
        List<Post> posts = new ArrayList<>();

        String sql = """
    SELECT 
        p.id_post, p.titre, p.description, p.date_publication, p.date_expiration, p.statut, p.pack_id, p.image_url,
        pv.nomPack, pv.prix_total, pv.details, pv.image_url AS pack_image, pv.nombre_places_restants,
        v.id_vol, v.numero_vol, v.compagnie, v.aero_depart, v.aero_arrivee, v.prix, v.date_depart, v.date_arrivee, v.disponible,
        h.id_hotel, h.nom AS hotel_nom, h.adresse AS hotel_adresse,
        e.id_excursion, e.nom AS excursion_nom, e.lieu AS excursion_lieu
    FROM Post p
    LEFT JOIN PackVoyage pv ON p.pack_id = pv.id_pack
    LEFT JOIN Vol v ON pv.vol_id = v.id_vol
    LEFT JOIN Hotel h ON pv.hotel_id = h.id_hotel
    LEFT JOIN Excursion e ON pv.excursion_id = e.id_excursion
    """;

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Créer vol
                vol v = new vol(
                        rs.getInt("id_vol"),
                        rs.getString("numero_vol"),
                        rs.getString("compagnie"),
                        rs.getString("aero_depart"),
                        rs.getString("aero_arrivee"),
                        rs.getDouble("prix"),
                        rs.getDate("date_depart"),
                        rs.getDate("date_arrivee")
                );

                // Créer hôtel
                Hotel h = new Hotel(
                        rs.getInt("id_hotel"),
                        rs.getString("hotel_nom"),
                        rs.getString("hotel_adresse")
                );

                // Créer excursion
                Excursion e = new Excursion(
                        rs.getInt("id_excursion"),
                        rs.getString("excursion_nom"),
                        rs.getString("excursion_lieu")
                );

                // Créer pack
                PackVoyage packVoyage = new PackVoyage(
                        rs.getInt("pack_id"),
                        rs.getString("nomPack"),
                        rs.getDouble("prix_total"),
                        rs.getString("details"),
                        rs.getInt("nombre_places_restants"),
                        rs.getString("pack_image"),
                        v, h, e
                );

                // Statut automatique
                StatutPost statut = (packVoyage.getNombrePlacesRestants() == 0)
                        ? StatutPost.EXPIRÉ
                        : StatutPost.DISPONIBLE;

                // Créer post
                Post post = new Post(
                        rs.getInt("id_post"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getDate("date_expiration"),
                        packVoyage,
                        statut,
                        rs.getString("image_url")
                );

                posts.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de la récupération des Posts", e);
        }

        return posts;
    }

    // Méthode pour mettre à jour le statut du Post dans la base de données





    // Trouver un post par son ID
    @Override
    public Post trouverParId(int id) {
        String sql = "SELECT * FROM Post WHERE id_post = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Retourner un Post avec ses données et son PackVoyage associé
                Post post = new Post(
                        rs.getInt("id_post"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getDate("date_expiration"),
                        null,  // Vous pouvez ajouter un PackVoyage ici si nécessaire
                        rs.getString("image_url")
                );
                return post;
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de la recherche du Post par ID", e);
        }

        return null;
    }

    // Modifier un post
    public void modifierPost(Post post) {
        if (post.getIdPost() == 0) {
            throw new RuntimeException("❌ Erreur : Impossible de modifier un Post sans ID !");
        }

        // Vérification de l'état du pack et mise à jour du statut du post
        if (post.getPackVoyage() != null && post.getPackVoyage().getNombrePlacesRestants() == 0) {
            post.setStatut(StatutPost.EXPIRÉ);  // Si plus de places disponibles, le statut devient EXPIRÉ
        } else if (post.getPackVoyage() != null && post.getPackVoyage().getNombrePlacesRestants() > 0) {
            post.setStatut(StatutPost.DISPONIBLE);  // Si des places sont encore disponibles, le statut devient DISPONIBLE
        }

        // SQL pour mettre à jour le statut du Post
        String sql = "UPDATE Post SET statut = ?, pack_id = ? WHERE id_post = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, post.getStatut().name());  // Update the Post's status
            stmt.setInt(2, post.getPackVoyage() != null ? post.getPackVoyage().getId() : 0); // Update the pack ID if needed
            stmt.setInt(3, post.getIdPost());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("⚠️ Aucun Post modifié, vérifiez l'ID !");
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de la modification du Post", e);
        }
    }



    // Supprimer un post
    @Override
    public void supprimerPost(Post post) {
        String sql = "DELETE FROM Post WHERE id_post = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, post.getIdPost());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de la suppression du Post", e);
        }
    }

    // Vérifier le statut des posts (par exemple, mettre à jour le statut des posts expirés)
    @Override
    public void verifierStatutPosts() {
        String sql = """
            UPDATE Post 
            SET statut = 'EXPIRÉ' 
            WHERE pack_id IN (SELECT id_pack FROM PackVoyage WHERE nombre_places_restants = 0)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de la mise à jour des Posts expirés", e);
        }
    }


}
