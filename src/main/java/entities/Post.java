package entities;

import java.util.Date;

public class Post {
    private int idPost;
    private String titre;
    private String description;
    private Date datePublication;
    private Date dateExpiration;
    private StatutPost statut;
    private PackVoyage packVoyage; // Relation objet
    private Utilisateur admin; // Relation objet
    private String imageUrl;

    // Constructeur pour la création d’un post
    public Post(int idPost, String titre, String description, Date dateExpiration, PackVoyage packVoyage, Utilisateur admin, String imageUrl) {
        this.idPost = idPost;
        this.titre = titre;
        this.description = description;
        this.datePublication = new Date();
        this.dateExpiration = dateExpiration;
        this.statut = StatutPost.DISPONIBLE;
        this.packVoyage = packVoyage;
        this.admin = admin;
        this.imageUrl = imageUrl;
    }
    // ✅ Constructeur avec ID (Récupération depuis la base de données)
    public Post(int idPost, String titre, String description, Date dateExpiration, PackVoyage packVoyage, Utilisateur admin) {
        this.idPost = idPost;
        this.titre = titre;
        this.description = description;
        this.datePublication = new Date();
        this.dateExpiration = dateExpiration;
        this.statut = StatutPost.DISPONIBLE;
        this.packVoyage = packVoyage;
        this.admin = admin;
    }

    public Post(String titre, String description, Date dateExpiration, PackVoyage packVoyage, Utilisateur admin, String imageUrl) {
        this.titre = titre;
        this.description = description;
        this.datePublication = new Date(); // Date actuelle
        this.dateExpiration = dateExpiration;
        this.statut = StatutPost.DISPONIBLE;
        this.packVoyage = packVoyage;
        this.admin = admin;
        this.imageUrl = imageUrl; // ✅ Ajout de l'image
    }

    public Post(int idPost, String titre, String description, java.sql.Date dateExpiration, PackVoyage packVoyage, String imageUrl) {
        this.idPost = idPost;              // Initialise l'ID du Post
        this.titre = titre;                // Initialise le titre du Post
        this.description = description;    // Initialise la description du Post
        this.dateExpiration = dateExpiration; // Initialise la date d'expiration du Post
        this.packVoyage = packVoyage;      // Associe l'objet PackVoyage au Post
        this.imageUrl = imageUrl;          // Associe l'URL de l'image du Post
    }
    public Post(int idPost, String titre, String description, Date dateExpiration, PackVoyage packVoyage, StatutPost statut, String imageUrl) {
        this.idPost = idPost;
        this.titre = titre;
        this.description = description;
        this.dateExpiration = dateExpiration;
        this.packVoyage = packVoyage;
        this.statut = statut;  // Affectation du statut
        this.imageUrl = imageUrl;
    }

    public Post(int postId, StatutPost postStatut) {
        this.idPost = postId;
        this.statut = postStatut;
    }

    public Post(int idPost, String titre, String description) {
        this.idPost = idPost;
        this.titre = titre;
        this.description = description;
    }


    // Getters et Setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDatePublication() { return datePublication; }
    public void setDatePublication(Date datePublication) { this.datePublication = datePublication; }

    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }

    public StatutPost getStatut() { return statut; }
    public void setStatut(StatutPost statut) { this.statut = statut; }

    public PackVoyage getPackVoyage() { return packVoyage; }
    public void setPackVoyage(PackVoyage packVoyage) { this.packVoyage = packVoyage; }

    public Utilisateur getAdmin() { return admin; }
    public void setAdmin(Utilisateur admin) { this.admin = admin; }

    // Vérifier si le post est expiré
    public boolean estExpire() {
        return new Date().after(this.dateExpiration);
    }

    // Mettre à jour automatiquement le statut du post
    public void mettreAJourStatut() {
        if (estExpire()) {
            this.statut = StatutPost.EXPIRÉ;
        }
    }

    @Override
    public String toString() {
        return "Post{" +
                "packVoyage=" + packVoyage +
                '}';
    }

    public int getIdPost() {
        return idPost;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Post(int idPost, PackVoyage packVoyage, Date dateExpiration, String imageUrl) {
        this.idPost = idPost;
        this.packVoyage = packVoyage;
        this.dateExpiration = dateExpiration;
        this.imageUrl = imageUrl;
    }

    public Post(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
}
