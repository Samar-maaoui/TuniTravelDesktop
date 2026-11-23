package entities;

public class Evenement {
    private int idEvenement;
    private String nom;
    private String lieu;
    private String dateEvenement;
    private int capacite;
    private String type;
    private String organisateur;
    private String description;
    private double prix;

    // Constructor
    public Evenement(int idEvenement, String nom, String lieu, String dateEvenement, int capacite,
                     String type, String organisateur, String description, double prix) {

        this.idEvenement = idEvenement;
        this.nom = nom;
        this.description = description;
        this.dateEvenement = dateEvenement;
        this.lieu = lieu;
        this.prix = prix;
        this.organisateur = organisateur;
        this.capacite = capacite;
        this.type = type;
    }

    public Evenement(int idEvenement, String nom, String description, String dateEvenement, String lieu, int capacite, String type, String organisateur, double prix) {
    }

    public Evenement(int i, String nom, String description, String dateEvenement, String lieu, String s, double prix, String organisateur, int capacite, String type) {
    }


    // Getters and Setters
    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(String dateEvenement) { this.dateEvenement = dateEvenement; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOrganisateur() { return organisateur; }
    public void setOrganisateur(String organisateur) { this.organisateur = organisateur; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public Evenement(int idEvenement, String type, String organisateur) {
        this.idEvenement = idEvenement;
        this.type = type;
        this.organisateur = organisateur;
    }
}
