package be.helha.labos.crystalclash.DAOImpl;


import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.RegistreDAO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class RegistreDAOimpl implements RegistreDAO{

    /**
     * @param username
     * Va dans la table users, select les users pour voir déjç ceux crées
     * */
    @Override
        public boolean userExists(String username) throws Exception {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }


    /**
     * @param username
     * @param hashedPassword
     * Va ajouter le user inscrit dans la db avec son username et password haché
     * initialise le niveau a 1 et cristaux a 100
     * */
    @Override
    public void insertUser(String username, String hashedPassword) throws Exception {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, level, cristaux,is_connected,gagner,perdu,Winconsecutive) VALUES (?, ?, ?, ?,?,?,?,?)");
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setInt(3, 1);
            stmt.setInt(4, 100);
            stmt.setBoolean(5, false);//false direct car a l'inscription le user est co instante
            stmt.setInt(6, 0);
            stmt.setInt(7, 0);
            stmt.setInt(8, 0);
            stmt.executeUpdate();
        }
    }
}
