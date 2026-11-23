
package services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import entities.*;
import utils.MyDatabase;


public class c_vol implements vole <vol> {
    Connection connection;

    public c_vol() {
        connection = MyDatabase.getInstance().getConnection();
    }
    private static c_vol instance;

    public static c_vol getInstance() {
        if (instance == null) {
            instance = new c_vol();
        }
        return instance;
    }

   @Override
   public void ajouter(vol vole) throws SQLException {
       String req = "INSERT INTO vol (disponible, numero_vol, compagnie, aero_depart, aero_arrivee, prix, date_depart, date_arrivee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

       try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
           preparedStatement.setInt(1, vole.getDisponible());
           preparedStatement.setString(2, vole.getnumero_vol());
           preparedStatement.setString(3, vole.getCompagnie());
           preparedStatement.setString(4, vole.getaero_depart());
           preparedStatement.setString(5, vole.getaero_arrivee());
           preparedStatement.setDouble(6, vole.getPrix());

           // Vérification des dates avant insertion
           if (vole.getDate_depart() != null) {
               preparedStatement.setDate(7, new java.sql.Date(vole.getDate_depart().getTime()));
           } else {
               preparedStatement.setNull(7, java.sql.Types.DATE);
           }

           if (vole.getDate_arrivee() != null) {
               preparedStatement.setDate(8, new java.sql.Date(vole.getDate_arrivee().getTime()));
           } else {
               preparedStatement.setNull(8, java.sql.Types.DATE);
           }

           preparedStatement.executeUpdate();
           System.out.println("Vol ajouté avec succès !");
       } catch (SQLException e) {
           System.err.println("Erreur lors de l'ajout du vol : " + e.getMessage());
           throw e;
       }
   }




    @Override
    public void modifier(vol vole) throws SQLException {
        String req = "UPDATE vol SET disponible=?, compagnie=?, aero_depart=?, aero_arrivee=?, prix=?, date_depart=?, date_arrivee=? WHERE numero_vol=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
           // preparedStatement.setInt(1, vole.getId_vol()); // Assuming correct getter

            preparedStatement.setInt(1, vole.getDisponible());
            preparedStatement.setString(2, vole.getCompagnie());
            preparedStatement.setString(3, vole.getaero_depart()); // Missing in the original code
            preparedStatement.setString(4, vole.getaero_arrivee()); // Missing in the original code
            preparedStatement.setDouble(5, vole.getPrix());
            preparedStatement.setDate(6, new java.sql.Date(vole.getDate_depart().getTime()));
            preparedStatement.setDate(7, new java.sql.Date(vole.getDate_arrivee().getTime()));
            preparedStatement.setString(8, vole.getnumero_vol()); // Assuming correct getter

            int rowsUpdated = preparedStatement.executeUpdate();
            System.out.println(rowsUpdated > 0 ? "Vol modifié avec succès!" : "Aucun vol trouvé avec ce nom.");
        }
    }
    @Override
    public void supprimer(String num) throws SQLException {
        String req = "DELETE FROM vol WHERE numero_vol=?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setString(1, num);

            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Vol supprimé avec succès!");
            } else {
                System.out.println("Aucun vol trouvé avec cet ID.");
            }
        }
    }
    public void supprimerVol(vol v) throws SQLException {
        String sql = "DELETE FROM vol WHERE id_vol = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, v.getId_vol());  // Utilise l'ID du vol
            preparedStatement.executeUpdate();
            System.out.println("✅ Vol supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du vol : " + e.getMessage());
        }
    }




    @Override

    public List<vol> afficher() throws SQLException {
        List<vol> vols = new ArrayList<>();
        String req = "SELECT * FROM vol";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(req)) {

            while (resultSet.next()) {
                vol v = new vol(
                        resultSet.getInt("disponible"),
                        resultSet.getString("numero_vol"),
                        resultSet.getString("compagnie"),
                        resultSet.getString("aero_depart"),
                        resultSet.getString("aero_arrivee"),
                        resultSet.getDouble("prix"),
                        resultSet.getDate("date_depart"),
                        resultSet.getDate("date_arrivee")
                );

                System.out.println("✅ Vol trouvé : " + v.getnumero_vol() + " - " + v.getaero_depart() + " → " + v.getaero_arrivee()); // DEBUG
                vols.add(v);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'affichage : " + e.getMessage());
        }

        System.out.println("🔍 Total vols récupérés : " + vols.size()); // DEBUG
        return vols;
    }

    public List<vol> getAllvols() throws SQLException {
        List<vol> vols = new ArrayList<>();
        String query = "SELECT * FROM vol";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            vol v = new vol();
            v.setId_vol(resultSet.getInt("id_vol"));
            v.setDisponible(resultSet.getInt("disponible"));// ✅ Récupération de l'ID du vol
            v.setnumero_vol(resultSet.getString("numero_vol"));
            v.setCompagnie(resultSet.getString("compagnie"));
            v.setPrix(resultSet.getDouble("prix"));
            v.setDate_depart(resultSet.getDate("date_depart"));
            v.setDate_arrivee(resultSet.getDate("date_arrivee"));

            // ✅ Ajout de l'aéroport de départ et d'arrivée
            v.setAero_depart(resultSet.getString("aero_depart"));
            v.setAero_arriveea(resultSet.getString("aero_arrivee"));

            System.out.println("✅ Vol chargé : " + v.getnumero_vol() + " (" + v.getaero_depart() + " → " + v.getaero_arrivee() + ")");

            vols.add(v);
        }

        return vols;
    }



    public int findidbynumvol(String numero_vol) throws SQLException {
        String req = "SELECT id_vol FROM vol WHERE numero_vol = ?";
        PreparedStatement statement = connection.prepareStatement(req);
        statement.setString(1, numero_vol); // ✅ Correction : numero_vol est une chaîne
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return rs.getInt("id_vol");
        }

        return -1; // Retourne -1 si le vol n'est pas trouvé
    }


    public vol getvolbyid(int id) throws SQLException {
        String req = "SELECT * FROM vol WHERE id_vol=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                vol v = new vol();
                v.setId_vol(resultSet.getInt("id_vol"));
                v.setnumero_vol(resultSet.getString("numero_vol"));
                v.setCompagnie(resultSet.getString("compagnie"));
                v.setPrix(resultSet.getDouble("prix"));
                v.setDate_depart(resultSet.getDate("date_depart"));
                v.setDate_arrivee(resultSet.getDate("date_arrivee"));

                // ✅ Ajout des valeurs aero_depart et aero_arrivee
                v.setAero_depart(resultSet.getString("aero_depart"));
                v.setAero_arriveea(resultSet.getString("aero_arrivee"));

                System.out.println("✅ Vol récupéré avec ID : " + v.getId_vol() + " - " + v.getnumero_vol() +
                        " (" + v.getaero_depart() + " → " + v.getaero_arrivee() + ")");

                return v;
            } else {
                System.err.println("❌ Vol introuvable pour ID: " + id);
            }
        }
        return null; // Retourne null si le vol n'est pas trouvé
    }



    public List<String> lister() throws SQLException {
        List<String> volsList = new ArrayList<>();

        // Récupérer tous les vols à l'aide de getAllvols()
        List<vol> vols = getAllvols();

        for (vol v : vols) {
            String volInfo = v.getnumero_vol() + " - " +
                    v.getCompagnie() + " - " +
                    v.getaero_depart() + " → " +
                    v.getaero_arrivee();
            volsList.add(volInfo);
        }

        return volsList;
    }









}

