package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import be.helha.labos.crystalclash.DAO.*;

@Repository
public class UserDAOImpl implements UserDAO {

    /*
    * @param username
    * Optinal pour dire qu'on s'est bien co, la requete a ete executée mais le resultat est vide
    * */
    @Override
    public Optional<Map<String,Object>> getUserInfo(String username) throws Exception{

        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT level, cristaux FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", username);
                userInfo.put("level", rs.getInt("level"));
                userInfo.put("cristaux", rs.getInt("cristaux"));
                return Optional.of(userInfo);
            } else {
                return Optional.empty();
            }
        }
    }
}
