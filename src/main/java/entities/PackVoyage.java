package entities;

public class PackVoyage {
    private int id;
    private String nomPack;
    private Utilisateur admin;
    private vol vol;
    private Hotel hotel;
    private Excursion excursion;
    private double prixTotal;
    private String details;
    private int nombrePlacesRestants;
    private String imageUrl;

    // ✅ Constructeur sans ID (pour l'ajout)
    public PackVoyage(String nomPack, Utilisateur admin, vol vol, Hotel hotel, Excursion excursion, double prixTotal, String details, int nombrePlacesRestants, String imageUrl) {
        if (vol == null || hotel == null || excursion == null) {
            throw new IllegalArgumentException("❌ Un PackVoyage doit contenir un vol, un hôtel et une excursion !");
        }
        this.nomPack = nomPack;
        this.admin = admin;
        this.vol = vol;
        this.hotel = hotel;
        this.excursion = excursion;
        this.prixTotal = prixTotal;
        this.details = details;
        this.nombrePlacesRestants = nombrePlacesRestants;
        this.imageUrl = imageUrl;
    }

    // ✅ Constructeur avec ID (pour la récupération depuis la base de données)
    public PackVoyage(int id, String nomPack, Utilisateur admin, vol vol, Hotel hotel, Excursion excursion, double prixTotal, String details, int nombrePlacesRestants, String imageUrl) {
        this(nomPack, admin, vol, hotel, excursion, prixTotal, details, nombrePlacesRestants, imageUrl);
        this.id = id;
    }

    // ✅ Constructeur pour mise à jour avec image et places restantes
    public PackVoyage(double prixTotal, String details, int nombrePlacesRestants, String imageUrl) {
        this.prixTotal = prixTotal;
        this.details = details;
        this.nombrePlacesRestants = nombrePlacesRestants;
        this.imageUrl = imageUrl;
    }

    // ✅ Constructeur pour mise à jour avec image et détails
    public PackVoyage(String imageUrl, int nombrePlacesRestants, String details) {
        this.imageUrl = imageUrl;
        this.nombrePlacesRestants = nombrePlacesRestants;
        this.details = details;
    }

    // ✅ Constructeur par défaut
    public PackVoyage() {
    }
    public PackVoyage(String nomPack, vol vol, Hotel hotel, Excursion excursion, double prixTotal,
                      String details, int nombrePlacesRestants, String imageUrl) {
        this.nomPack = nomPack;  // Nom du pack
        this.vol = vol;  // Vol associé au pack
        this.hotel = hotel;  // Hôtel associé au pack
        this.excursion = excursion;  // Excursion associée au pack
        this.prixTotal = prixTotal;  // Prix total du pack
        this.details = details;  // Détails du pack
        this.nombrePlacesRestants = nombrePlacesRestants;  // Nombre de places restantes
        this.imageUrl = imageUrl;  // URL de l'image associée au pack
    }




    public PackVoyage(int idPack, String nomPack, vol vol, Hotel hotel, Excursion excursion, double prixTotal, String details, int nombrePlacesRestants, String imageUrl) {
        this.id = idPack;
        this.nomPack = nomPack;
        this.vol = vol;
        this.hotel = hotel;
        this.excursion = excursion;
        this.prixTotal = prixTotal;
        this.details = details;
        this.nombrePlacesRestants = nombrePlacesRestants;
        this.imageUrl = imageUrl;
    }

    public PackVoyage(int packId, String nomPack, double prixTotal, String details, int nombrePlacesRestantes, String imageUrl, entities.vol vol, Hotel hotel, Excursion excursion) {
        this.id = packId; // Initialisation de l'ID du pack
        this.nomPack = nomPack; // Initialisation du nom du pack
        this.prixTotal = prixTotal; // Initialisation du prix total
        this.details = details; // Initialisation des détails du pack
        this.nombrePlacesRestants= nombrePlacesRestantes; // Initialisation du nombre de places restantes
        this.imageUrl = imageUrl; // Initialisation de l'URL de l'image
        this.vol = vol; // Initialisation de l'objet vol
        this.hotel = hotel; // Initialisation de l'objet hôtel
        this.excursion = excursion; // Initialisation de l'objet excursion
    }

    public PackVoyage(int id, String nom, String description, double prix, Hotel hotel, entities.vol vol, Excursion excursion) {
        this.id = id;  // Initialisation de l'ID du pack
        this.nomPack = nom;  // Initialisation du nom du pack
        this.details = description;  // Initialisation de la description du pack
        this.prixTotal = prix;  // Initialisation du prix total du pack
        this.hotel = hotel;  // Initialisation de l'hôtel associé au pack
        this.vol = vol;  // Initialisation du vol associé au pack
        this.excursion = excursion;  // Initialisation de l'excursion associée au pack
    }

    public PackVoyage(int id, String nom, String description, double prix, Hotel hotel, entities.vol vol, Excursion excursion, int nombrePlacesRestants, Utilisateur admin, String imageUrl) {
        this.id = id;                             // ID du pack voyage
        this.nomPack = nom;                       // Nom du pack
        this.details = description;               // Description du pack
        this.prixTotal = prix;                    // Prix total du pack
        this.hotel = hotel;                       // L'hôtel associé au pack
        this.vol = vol;                           // Le vol associé au pack
        this.excursion = excursion;               // L'excursion associée au pack
        this.nombrePlacesRestants = nombrePlacesRestants; // Nombre de places restantes pour le pack
        this.admin = admin;                       // L'admin qui a créé ce pack
        this.imageUrl = imageUrl;                 // URL de l'image du pack
    }

    public PackVoyage(int packId) {
    }

    public PackVoyage(int idPack, String nomPack, double prixTotal, int nombrePlacesRestants) {
        this.id = idPack;                  // Initialise l'ID du Pack
        this.nomPack = nomPack;                // Initialise le nom du Pack
        this.prixTotal = prixTotal;            // Initialise le prix total du Pack
        this.nombrePlacesRestants = nombrePlacesRestants;  // Initialise le nombre de places restantes
    }


    // ✅ Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomPack() {
        return nomPack;
    }

    public void setNomPack(String nomPack) {
        this.nomPack = nomPack;
    }

    public Utilisateur getAdmin() {
        return admin;
    }

    public void setAdmin(Utilisateur admin) {
        this.admin = admin;
    }

    public vol getVol() {
        return vol;
    }

    public void setVol(vol vol) {
        this.vol = vol;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Excursion getExcursion() {
        return excursion;
    }

    public void setExcursion(Excursion excursion) {
        this.excursion = excursion;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getNombrePlacesRestants() {
        return nombrePlacesRestants;
    }

    public void setNombrePlacesRestants(int nombrePlacesRestants) {
        this.nombrePlacesRestants = nombrePlacesRestants;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return nomPack;
    }

    public PackVoyage(int id, String details, double prixTotal, Excursion excursion, String imageUrl) {
        this.id = id;
        this.details = details;
        this.prixTotal = prixTotal;
        this.excursion = excursion;
        this.imageUrl = imageUrl;
    }

    public PackVoyage(int id, String details, Hotel hotel) {
        this.id = id;
        this.details = details;
        this.hotel = hotel;
    }

    public PackVoyage(int nombrePlacesRestants, String imageUrl) {
        this.nombrePlacesRestants = nombrePlacesRestants;
        this.imageUrl = imageUrl;
    }
    public PackVoyage(int id, String nomPack, double prixTotal, String imageUrl) {
        this.id = id;
        this.nomPack = nomPack;
        this.prixTotal = prixTotal;
        this.imageUrl = imageUrl;
    }

    public PackVoyage(Utilisateur admin) {
        this.admin = admin;
    }
}
