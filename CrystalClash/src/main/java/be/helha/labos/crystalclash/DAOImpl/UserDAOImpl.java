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
            PreparedStatement stmt = conn.prepareStatement("SELECT username, level, cristaux,is_connected, gagner,perdu FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserInfo user = new UserInfo();
                user.setUsername(rs.getString("username"));
                user.setLevel(rs.getInt("level"));
                user.setCristaux(rs.getInt("cristaux"));
                user.setConnected(rs.getBoolean("is_connected"));
                user.setGagner(rs.getInt("gagner"));
                user.setPerdu(rs.getInt("perdu"));

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
    public void updateCristaux(String username, int newCristaux)throws Exception  {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET cristaux = ? WHERE username = ?");
            stmt.setInt(1, newCristaux);
            stmt.setString(2, username);
            int updatedRows = stmt.executeUpdate();

            if (updatedRows == 0) {
                throw new IllegalStateException("Aucun utilisateur mis à jour : " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /*
    * Va check si le user est deja co dans la db.
    * */
    @Override
    public boolean isAlreadyConnected(String username) throws Exception {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT is_connected FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_connected");
            }
            return false;
        }
    }

    //Va mettre a jour le boolean de is_connected
    @Override
    public void updateIsConnected(String username, boolean isConnected)throws Exception{
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET is_connected = ? WHERE username = ?"
            );
            stmt.setBoolean(1, isConnected);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateLevel(String username, int newLevel) throws Exception {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET level = ? WHERE username = ?");
            stmt.setInt(1, newLevel);
            stmt.setString(2, username);
            int updatedRows = stmt.executeUpdate();

            if (updatedRows == 0) {
                throw new IllegalStateException("Aucun utilisateur mis à jour : " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override

    public void IncrementWinner(String username) throws Exception {
        updateWin_Lose(username, "gagner");
    }

    public void IncrementDefeat(String username) throws Exception {
        updateWin_Lose(username, "perdu");
    }

    @Override
    public void updateWin_Lose(String username, String New) throws Exception {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET " + New  +" = " + New + " + 1 where username = ?");
            stmt.setString(1, username);
            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw new IllegalStateException("Aucun utilisateur mis à jour : " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
