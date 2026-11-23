package services;

import entities.PackVoyage;
import entities.Post;
import entities.TypeUtilisateur;
import entities.Utilisateur;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPVService {
    private final Connection connection;

    public AdminPVService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    // 🔹 1. Top 3 des packs les plus réservés
    public List<PackVoyage> getTopPacksParReservations() throws SQLException {
        List<PackVoyage> topPacks = new ArrayList<>();
        String sql = """
            SELECT p.*, COUNT(r.id_reservation) AS total_reservations
            FROM packvoyage p
            JOIN reservationpackvoyage r ON p.id_pack = r.pack_id
            GROUP BY p.id_pack
            ORDER BY total_reservations DESC
            LIMIT 3
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            PackVoyageService packService = PackVoyageService.getInstance();
            while (rs.next()) {
                PackVoyage pack = packService.trouverPackParId(rs.getInt("id_pack"));
                topPacks.add(pack);
            }
        }
        return topPacks;
    }

    // 🔹 5. Packs visibles selon le type de l'utilisateur connecté
    public List<PackVoyage> filtrerPacksSelonUtilisateur(Utilisateur utilisateur) throws SQLException {
        PackVoyageService packService = PackVoyageService.getInstance();
        List<PackVoyage> tous = packService.listerPacks();
        List<PackVoyage> filtres = new ArrayList<>();

        for (PackVoyage p : tous) {
            if (utilisateur.getTypeUtilisateur() == TypeUtilisateur.VIP) {
                filtres.add(p); // VIP voit tout
            } else {
                if (p.getNombrePlacesRestants() > 0) {
                    filtres.add(p); // Client normal : uniquement packs disponibles
                }
            }
        }
        return filtres;
    }

    // 🔹 6. Vérifier disponibilité avant réservation
    public boolean verifierDisponibiliteAvantReservation(int packId, int quantiteDemandee) throws SQLException {
        String sql = "SELECT nombre_places_restants FROM packvoyage WHERE id_pack = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, packId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("nombre_places_restants") >= quantiteDemandee;
            }
        }
        return false;
    }

    // 🔹 7. Lister uniquement les posts actifs (pas expirés et places disponibles)
    public List<Post> listerPostsActifs() {
        List<Post> actifs = new ArrayList<>();
        String sql = """
            SELECT p.id_post
            FROM Post p
            JOIN packvoyage pv ON p.pack_id = pv.id_pack
            WHERE p.date_expiration > NOW() AND pv.nombre_places_restants > 0
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            PostService postService = PostService.getInstance();
            while (rs.next()) {
                Post post = postService.trouverParId(rs.getInt("id_post"));
                actifs.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur dans listerPostsActifs", e);
        }
        return actifs;
    }
    public int getNombreReservationsPack(int idPack) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservationpackvoyage WHERE pack_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPack);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

}
