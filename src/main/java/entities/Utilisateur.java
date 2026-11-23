package entities;

import utils.PasswordUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Utilisateur {
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private int cin;
    private String email;
    private String motDePasse;
    private String adresse;
    private String telephone;
    private LocalDate dateNaissance;
    private TypeUtilisateur typeUtilisateur;

    public Utilisateur(int utilisateurId) {
        this.idUtilisateur = utilisateurId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    private String avatarUrl;
    private boolean isVerified; // ✅ Correction du nom de l'attribut


    public Utilisateur(String nom, String prenom, int cin, String email, String motDePasse, String adresse, String telephone, LocalDate dateNaissance, TypeUtilisateur typeUtilisateur, boolean isVerified) {
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.email = email;
        this.motDePasse = motDePasse;
        this.adresse = adresse;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.typeUtilisateur = typeUtilisateur;
        this.isVerified = isVerified;
    }

    // 🔹 Getters et Setters
    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    private List<Feedback> feedbacks; // 🔗 Liste de feedbacks

    public Utilisateur() {

    }


    public int getCin() {
        return cin;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setCin(int cin) {
        this.cin = cin;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }


    // 📌 Constructeur complet


    public Utilisateur(String nom, String prenom, int cin, String email, String motDePasse, String adresse, String telephone, LocalDate dateNaissance, TypeUtilisateur typeUtilisateur) {
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.email = email;
        this.motDePasse = motDePasse;
        this.adresse = adresse;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.typeUtilisateur = typeUtilisateur;
    }
    public Utilisateur(int id, String nom) {
        this.idUtilisateur = id;
        this.nom = nom;
    }
    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public TypeUtilisateur getTypeUtilisateur() {
        return typeUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = PasswordUtils.hashPassword(motDePasse);
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setTypeUtilisateur(TypeUtilisateur typeUtilisateur) {
        this.typeUtilisateur = typeUtilisateur;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", cin=" + cin +
                ", email='" + email + '\'' +
                ", motDePasse='" + motDePasse + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", typeUtilisateur=" + typeUtilisateur +
                ", isVerified=" + isVerified +
                ", feedbacks=" + feedbacks +
                '}';
    }

    public Utilisateur(int idUtilisateur, String nom, String prenom, int cin, String email,
                       String motDePasse, String adresse, String telephone,
                       LocalDate dateNaissance, TypeUtilisateur typeUtilisateur, boolean isVerified) {
        this.idUtilisateur = idUtilisateur;  // ✅ S'assurer que l'affectation est correcte
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.email = email;
        this.motDePasse = motDePasse;
        this.adresse = adresse;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.typeUtilisateur = typeUtilisateur;
        this.isVerified = isVerified;
    }

    public Utilisateur(String nom, String prenom, int cin, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.email = email;
    }
    public boolean estAdministrateur() {
        return this.typeUtilisateur.equals("ADMIN"); // Par exemple, si le rôle est un champ de type String
    }

    public void ajouterFeedback(Feedback feedback) {
        if (feedbacks == null) {
            feedbacks = new ArrayList<>();
        }
        feedbacks.add(feedback);
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void afficherFeedbacks() {
        System.out.println("Feedbacks de " + nom + " " + prenom + " :");
        for (Feedback fb : feedbacks) {
            System.out.println(fb);
        }
    }

    public Utilisateur(int idUtilisateur, String email, String motDePasse) {
        this.idUtilisateur = idUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
    }
}