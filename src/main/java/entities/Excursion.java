package entities;

import java.time.LocalDate;
import java.util.Date;

public class Excursion {
    private int idExcursion;
    private String nom;
    private String lieu;
    private String villeDepart; // Nouvelle propriété

    private double prix;
    private LocalDate dateDepart;
    private LocalDate dateRetour;
    private String description;
    private String image;
    private int nbMaxParticipants;
    private Guide guide;
    private boolean disponibilite;

    private int nbPlace;
    private String nomGuide;
    private String prenomGuide;

    // Constructeur sans id
    public Excursion(String nom, String lieu, double prix, LocalDate dateDepart, LocalDate dateRetour,
                     String description, String image, int nbMaxParticipants, Guide guide, int nbPlace , String villeDepart) {
        this.nom = nom;
        this.lieu = lieu;
        this.villeDepart = villeDepart;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.description = description;
        this.image = image;
        this.nbMaxParticipants = nbMaxParticipants;
        this.guide = guide;
        this.nbPlace = nbPlace;
    }

    public Excursion() {
    }

    // Constructeur avec id
    public Excursion(int idExcursion, String nom, String lieu, double prix, LocalDate dateDepart, LocalDate dateRetour,
                     String description, String image, Guide guide, int nbPlace , String villeDepart) {
        this.idExcursion = idExcursion;
        this.nom = nom;
        this.villeDepart= villeDepart;
        this.lieu = lieu;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.description = description;
        this.image = image;
        this.guide = guide;
        this.nbPlace = nbPlace;
    }


    public boolean isDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }


    public Excursion(String nomExcursion, String lieuExcursion, double prixExcursion, Guide guideObj, LocalDate parse , String villeDepart) {
        this.nom = nomExcursion;
        this.lieu = lieuExcursion;
        this.prix = prixExcursion;
        this.guide = guideObj;
        this.dateDepart = LocalDate.parse(parse.toString());
        this.villeDepart = villeDepart;

    }

    public Excursion(String nomExcursion, String lieuExcursion, double prixExcursion, Guide guideObj, LocalDate dateDepart, LocalDate dateRetour , String description,int nbMaxParticipants , String image , String villeDepart) {
   this.nom = nomExcursion;
    this.lieu = lieuExcursion;
    this.prix = prixExcursion;
    this.guide = guideObj;
    this.dateDepart = dateDepart;
    this.dateRetour = dateRetour;
    this.description = description;
    this.nbMaxParticipants = nbMaxParticipants;
    this.image = image;
    this.villeDepart = villeDepart;}

    public Excursion(int idExcursion, String nom, String lieu, double prix, String image , Guide guide ) {
        this.idExcursion=idExcursion;
        this.nom = nom;
        this.lieu = lieu;
        this.prix = prix;
        this.image = image;
        this.guide = guide;
    }

    public Excursion(int idExcursion, String excursionNom, String excursionLieu) {
        this.idExcursion = idExcursion;
        this.nom = excursionNom;
        this.lieu = excursionLieu;
    }

    public Excursion(int idExcursion, String nom, String lieu, double prix, LocalDate dateDepart, LocalDate dateRetour, String description, String image, int nombreMaxParticipants) {
        this.idExcursion = idExcursion;
        this.nom = nom;
        this.lieu = lieu;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.description = description;
        this.image = image;
        this.nbMaxParticipants = nombreMaxParticipants;
    }

    public Excursion(int idExcursion, String nom, String lieu, double prix, LocalDate dateDepart,
                     LocalDate dateRetour, String description, String image, int nombreMaxParticipants,
                     String villeDepart) {
        this.idExcursion = idExcursion;  // Assign idExcursion
        this.nom = nom;                  // Assign nom (name of the excursion)
        this.lieu = lieu;                // Assign lieu (location of the excursion)
        this.prix = prix;                // Assign prix (price of the excursion)
        this.dateDepart = dateDepart;    // Assign dateDepart (departure date)
        this.dateRetour = dateRetour;    // Assign dateRetour (return date)
        this.description = description;  // Assign description of the excursion
        this.image = image;              // Assign image for the excursion
        this.nbMaxParticipants = nombreMaxParticipants;  // Assign the max number of participants
        this.villeDepart = villeDepart;  // Assign villeDepart (departure city)
    }


    // Getters and Setters
    public String getNomGuide() {
        return nomGuide;
    }

    public void setNomGuide(String nomGuide) {
        this.nomGuide = nomGuide;
    }

    public String getPrenomGuide() {
        return prenomGuide;
    }

    public void setPrenomGuide(String prenomGuide) {
        this.prenomGuide = prenomGuide;
    }

    public int getNbPlace() {
        return nbPlace;
    }

    public void setNbPlace(int nbPlace) {
        this.nbPlace = nbPlace;
    }

    public int getIdExcursion() {
        return idExcursion;
    }

    public void setIdExcursion(int idExcursion) {
        this.idExcursion = idExcursion;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getVilleDepart() {
        return villeDepart;
    }

    public void setVilleDepart(String villeDepart) {
            this.villeDepart = villeDepart;
    }


    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
    }

    public LocalDate getDateRetour() {
        return dateRetour;
    }

    public void setDateRetour(LocalDate dateRetour) {
        this.dateRetour = dateRetour;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNbMaxParticipants() {
        return nbMaxParticipants;
    }

    public void setNbMaxParticipants(int nbMaxParticipants) {
        this.nbMaxParticipants = nbMaxParticipants;
    }

    public Guide getGuide() {
        return guide;
    }

    public void setGuide(Guide guide) {
        this.guide = guide;
    }

    @Override
    public String toString() {
        return "Excursion{" +
                "idExcursion=" + idExcursion +
                ", nom='" + nom + '\'' +
                ", lieu='" + lieu + '\'' +
                ", villeDepart='" + villeDepart + '\'' +
                ", prix=" + prix +
                ", dateDepart=" + dateDepart +
                ", dateRetour=" + dateRetour +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", nbMaxParticipants=" + nbMaxParticipants +
                ", guide=" + guide +
                ", nbPlace=" + nbPlace +
                ", nomGuide='" + nomGuide + '\'' +
                ", prenomGuide='" + prenomGuide + '\'' +
                '}';
    }

    public Excursion(int nbMaxParticipants, String image) {
        this.nbMaxParticipants = nbMaxParticipants;
        this.image = image;
    }
}
