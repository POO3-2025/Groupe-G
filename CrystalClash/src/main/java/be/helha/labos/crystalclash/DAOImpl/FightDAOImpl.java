package be.helha.labos.crystalclash.DAOImpl;


import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.FightDAO;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FightDAOImpl implements FightDAO {

    /**
     * Récupère la liste des utilisateurs triée par le nombre de victoires.
     *
     * @return Une liste d'objets UserInfo contenant les informations des utilisateurs.
     */
    @Override
    public List<UserInfo> getClassementPlayer(){
        List<UserInfo> list = new ArrayList<UserInfo>();
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT username,gagner FROM users ORDER BY gagner DESC ");
            ResultSet rs = checkStmt.executeQuery();

            while (rs.next()) {
                UserInfo user = new UserInfo();
                user.setUsername(rs.getString("username"));
                user.setGagner(rs.getInt("gagner"));
                list.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

}
