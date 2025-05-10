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
    void updateCristaux(String username, int newCristaux)throws Exception ;

     boolean isAlreadyConnected(String username) throws Exception;

     void updateIsConnected(String username, boolean isConnected)throws Exception;

    void updateLevel(String username, int newLevel) throws Exception;


    //deux nouvelles méthodes
    void IncrementWinner(String username) throws Exception;
    void IncrementDefeat(String username) throws Exception;
     void updateWin_Lose(String username, String New) throws Exception;

     //trophé
     void incrementWinconsecutive(String username) throws Exception;
    void resetWinconsecutiveConsecutive(String username) throws Exception;
    }
