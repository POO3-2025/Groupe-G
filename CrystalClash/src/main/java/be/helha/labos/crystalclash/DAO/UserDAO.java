package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.User.UserInfo;
import java.util.Optional;

public interface UserDAO {
    Optional<UserInfo> getUserByUsername(String username);
    void updateCristaux(String username, int newCristaux);
}
