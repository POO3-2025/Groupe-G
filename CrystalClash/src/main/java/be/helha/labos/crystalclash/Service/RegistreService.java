package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.RegistreDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistreService {

    private final RegistreDAO registreDAO;

    @Autowired
    public RegistreService(RegistreDAO registreDAO) {
        this.registreDAO = registreDAO;
    }

   public boolean userExists(String username) throws Exception{
        return registreDAO.userExists(username);
   }

   public void insertUser(String username, String hashedPassword) throws Exception{
        registreDAO.insertUser(username, hashedPassword);
   }

}
