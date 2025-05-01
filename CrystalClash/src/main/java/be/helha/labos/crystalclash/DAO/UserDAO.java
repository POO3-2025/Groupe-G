package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.User.UserInfo;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {
    /**
     * @param username
     * */
    Optional<UserInfo> getUserByUsername(String username);
    /**
     * @param username
     * @param newCristaux
     * */
    void updateCristaux(String username, int newCristaux);

     boolean isAlreadyConnected(String username) throws Exception;

     void updateIsConnected(String username, boolean isConnected)throws Exception;
}
