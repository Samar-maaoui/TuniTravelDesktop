package entities;


public class Hotel {
    private int id_hotel;
    private String nom;
    private double prix_nuit;
    private boolean est_disponible;
    private String adresse;
    private String description;
    private String tel;
    private String email_contact;
    private String image_hotel;

    public Hotel() {
    }



    public Hotel(int hotelId, String nom) {
    }

    public Hotel(int idHotel, String nom, double prixNuit, String adresse, String description) {
        this.id_hotel = idHotel;
        this.nom = nom;
        this.prix_nuit = prixNuit;
        this.adresse = adresse;
        this.description = description;
    }
    public Hotel(int idHotel, String nom, double prixNuit, boolean estDisponible, String adresse, String description, String tel, String emailContact, String imageHotel) {
        this.id_hotel = idHotel;
        this.nom = nom;
        this.prix_nuit = prixNuit;
        this.est_disponible = estDisponible;
        this.adresse = adresse;
        this.description = description;
        this.tel = tel;
        this.email_contact = emailContact;
        this.image_hotel = imageHotel;
    }


    public Hotel(int idHotel, String nom, double prixNuit, String adresse, String description, String tel, String emailContact, String imageHotel) {
        this.id_hotel = idHotel;
        this.nom = nom;
        this.prix_nuit = prixNuit;
        this.adresse = adresse;
        this.description = description;
        this.tel = tel;
        this.email_contact = emailContact;
        this.image_hotel = imageHotel;
    }


    public int getId_hotel() {
        return id_hotel;
    }

    public void setId_hotel(int id_hotel) {
        this.id_hotel = id_hotel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix_nuit() {
        return prix_nuit;
    }

    public void setPrix_nuit(double prix_nuit) {
        this.prix_nuit = prix_nuit;
    }

    public boolean isEst_disponible() {
        return est_disponible;
    }

    public void setEst_disponible(boolean est_disponible) {
        this.est_disponible = est_disponible;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail_contact() {
        return email_contact;
    }

    public void setEmail_contact(String email_contact) {
        this.email_contact = email_contact;
    }

    public String getImage_hotel() {
        return image_hotel;
    }

    public void setImage_hotel(String image_hotel) {
        this.image_hotel = image_hotel;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "id_hotel=" + id_hotel +
                ", nom='" + nom + '\'' +
                ", prix_nuit=" + prix_nuit +
                ", est_disponible=" + est_disponible +
                ", adresse='" + adresse + '\'' +
                ", description='" + description + '\'' +
                ", tel='" + tel + '\'' +
                ", email_contact='" + email_contact + '\'' +
                ", image_hotel='" + image_hotel + '\'' +
                '}';
    }

    public Hotel(String tel) {
        this.tel = tel;
    }
    public Hotel(int id, String nom, String adresse) {
        this.id_hotel = id;
        this.nom = nom;
        this.adresse = adresse;
    }

}
