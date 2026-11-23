package entities;

public class reservationvol {
    private int id_reservation;
    private int vol_id; // Clé étrangère vers la classe Vol
    private ClasseService classe_service; // Ajout de l'Enum
    private vol volDetails; // Objet vol associé
    private Utilisateur utilisateur; // Remplace l’ancien int utilisateur_id

    public vol getVolDetails() {
        return volDetails;
    }

    public void setVolDetails(vol volDetails) {
        this.volDetails = volDetails;
    }


    // Définition de l'Enum pour ClasseService
    public enum ClasseService {
        ÉCONOMIE, AFFAIRES, PREMIÈRE
    }

    public reservationvol(ClasseService classe_service) {
        this.classe_service = classe_service;

    }
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public reservationvol(int id_reservation, int vol_id, ClasseService classe_service, Utilisateur utilisateur) {
        this.id_reservation = id_reservation;
        this.vol_id = vol_id;
        this.classe_service = classe_service;
        this.utilisateur = utilisateur;
    }

    public reservationvol(int vol_id, ClasseService classe_service, Utilisateur utilisateur) {
        this.vol_id = vol_id;
        this.classe_service = classe_service;
        this.utilisateur = utilisateur;
    }


    public int getId_reservation() { return id_reservation; }
    public void setId_reservation(int id_reservation) { this.id_reservation = id_reservation; }

    public int getId_vol() { return vol_id; }
    public void setId_vol(int id_vol) { this.vol_id = id_vol; }

    public ClasseService getClasse_service() { return classe_service; }
    public void setClasse_service(ClasseService classe_service) { this.classe_service = classe_service; }

    @Override
    public String toString() {
        return "ReservationVol{" +
                "id_reservation=" + id_reservation +
                ", vol_id=" + vol_id +
                ", classe_service=" + classe_service +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getNom() : "null") +
                '}';
    }

}
