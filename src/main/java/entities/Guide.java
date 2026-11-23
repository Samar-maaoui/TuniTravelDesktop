package entities;

public class Guide {
    private int idGuide;
    private String nom;
    private String prenom;
    private String image;
    private String langue;
    private String telephone;
    private String email;

    public Guide(String nom, String prenom, String langue, String telephone, String email, String image) {
        this.nom = nom;
        this.prenom = prenom;
        this.langue = langue;
        this.telephone = telephone;
        this.email = email;
        this.image = image;
    }

    public Guide() {

    }
    public Guide(int idGuide, String nom, String prenom) {
        this.idGuide = idGuide;
        this.nom = nom;
        this.prenom = prenom;
    }

    public Guide(int idGuide, String nomGuide, String prenomGuide, String s, String s1, String s2, String s3) {
    }

    public Guide(String nomGuide, String prenomGuide) {
    }

    public int getIdGuide() {
        return idGuide;
    }

    public void setIdGuide(int idGuide) {
        this.idGuide = idGuide;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }



    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Guide{" +
                "idGuide=" + idGuide +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", langue='" + langue + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
