package services;

import entities.vol;

import java.sql.SQLException;
import java.util.List;

public interface vole<T>{
    public void ajouter(T t) throws SQLException;
    public void modifier(T t) throws SQLException;
    public void supprimer(String num) throws SQLException;
    public List<T> afficher() throws SQLException;
    List<vol> getAllvols() throws SQLException;

}