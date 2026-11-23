package entities;

import java.util.Date;

public class vol
{   private int id_vol,disponible;
    private String numero_vol,compagnie,aero_depart,aero_arrivee;
    private double prix;
    private Date date_depart,date_arrivee;
    public vol(){}
    public vol(int disponible,String numero_vol,String compagnie,String aero_depart,String aero_arrivee, double prix,Date date_depart,Date date_arrivee){

        this.disponible=disponible;
        this.numero_vol=numero_vol;
        this.compagnie=compagnie;
        this.aero_depart=aero_depart;
        this.aero_arrivee=aero_arrivee;
        this.prix=prix;
        this.date_depart=date_depart;
        this.date_arrivee=date_arrivee;
    }
    public vol(int id_vol,int disponible,String numero_vol,String compagnie,String aero_depart,String aero_arrivee, double prix,Date date_depart,Date date_arrivee){
        this.id_vol=id_vol;
        this.disponible=disponible;
        this.numero_vol=numero_vol;
        this.compagnie=compagnie;
        this.aero_depart=aero_depart;
        this.aero_arrivee=aero_arrivee;
        this.prix=prix;
        this.date_depart=date_depart;
        this.date_arrivee=date_arrivee;
    }

    public vol(int volId, String numeroVol, String compagnie) {
    }

    public vol(String numeroVol) {
        this.numero_vol=numeroVol;
    }

    // Constructor to initialize all fields
    public vol(int idVol, String numeroVol, String compagnie, double prix, String aeroDepart, String aeroArrivee) {
        this.id_vol = idVol;
        this.numero_vol = numeroVol;
        this.compagnie = compagnie;
        this.prix = prix;
        this.aero_depart = aeroDepart;
        this.aero_arrivee = aeroArrivee;
    }

    public int getId_vol() {return id_vol;}
    public void setId_vol(int id_vol) {this.id_vol = id_vol;}
    public int getDisponible() {return disponible;}
    public void setDisponible(int disponible){this.disponible=disponible;}
    public String getnumero_vol() {return numero_vol;}
    public void setnumero_vol(String numero_vol) {this.numero_vol = numero_vol;}
    public String getCompagnie() {return compagnie;}
    public void setCompagnie(String compagnie) {this.compagnie = compagnie;}
    public String getaero_depart() {return aero_depart;}
    public void setAero_depart(String Aero_depart){this.aero_depart = Aero_depart;}
    public String getaero_arrivee() {return aero_arrivee;}
    public void setAero_arriveea(String aero_arrivee){this.aero_arrivee = aero_arrivee;}
    public double getPrix() {return prix;}
    public void setPrix(double prix) {this.prix = prix;}
    public Date getDate_depart() {return date_depart;}
    public void setDate_depart(Date date_depart) {this.date_depart = date_depart;}
    public Date getDate_arrivee() {return date_arrivee;}
    public void setDate_arrivee(Date date_arrivee) {this.date_arrivee = date_arrivee;}

    @Override
    public String toString() {
        return "vol{" +
                "id_vol=" + id_vol +
                ", disponible=" + disponible +
                ", numero_vol='" + numero_vol + '\'' +
                ", compagnie='" + compagnie + '\'' +
                ", aero_depart='" + aero_depart + '\'' +
                ", aero_arrivee='" + aero_arrivee + '\'' +
                ", prix=" + prix +
                ", date_depart=" + date_depart +
                ", date_arrivee=" + date_arrivee +
                '}';
    }

    public vol(String aero_arrivee, double prix) {
        this.aero_arrivee = aero_arrivee;
        this.prix = prix;
    }

}
