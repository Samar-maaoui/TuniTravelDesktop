package services;

import entities.Excursion;

import java.sql.SQLException;
import java.util.List;

public interface Services<T> {
     public void ajouter(T t) throws SQLException;
    public boolean modifier(T t) throws SQLException;
    public void supprimer(int id) throws SQLException;




    List<T> lire() throws SQLException;
    public List<Excursion> lister() throws SQLException ;
    }
