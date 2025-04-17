package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<Map<String, Object>> getUserInfo(String username) throws Exception {
        return userDAO.getUserInfo(username);
    }
}
