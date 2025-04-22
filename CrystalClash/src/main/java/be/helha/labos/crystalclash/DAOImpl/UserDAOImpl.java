package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.UserDAO;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {

    /**
     * @param username
     * Obtenir les infos du joueur
     * Optional car les infos peut etre vide, rien trouvé au nom de l'uti
     * pour eviter les null, ptional.empty est utilisé
     * */
    @Override
    public Optional<UserInfo> getUserByUsername(String username) {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT username, level, cristaux FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserInfo user = new UserInfo();
                user.setUsername(rs.getString("username"));
                user.setLevel(rs.getInt("level"));
                user.setCristaux(rs.getInt("cristaux"));
                return Optional.of(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * @param username
     * @param newCristaux
     * update les cristaux du suer apres un achat ou une vente
     * */
    @Override
    public void updateCristaux(String username, int newCristaux) {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET cristaux = ? WHERE username = ?");
            stmt.setInt(1, newCristaux);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
