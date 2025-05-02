package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.UserDAO;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;


    /**
     * @param userDAO
     * **/
    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * @param username
     * **/
    public Optional<UserInfo> getUserInfo(String username) {
        return userDAO.getUserByUsername(username);
    }

    /**
     * @param username
     * @param newCristaux
     * **/
    public void updateCristaux(String username, int newCristaux)throws Exception  {
        userDAO.updateCristaux(username, newCristaux);
    }

     public boolean isAlreadyConnected(String username) throws Exception{
        return userDAO.isAlreadyConnected(username);
     }

     public void updateIsConnected(String username, boolean isConnected)throws Exception{
        userDAO.updateIsConnected(username, isConnected);
     }



}
