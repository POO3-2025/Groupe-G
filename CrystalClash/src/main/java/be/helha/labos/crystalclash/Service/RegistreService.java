package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.RegistreDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistreService {

    private final RegistreDAO registreDAO;

    /**
     * Constructeur avec injection du DAO du registre.
     *
     * @param registreDAO DAO pour accéder aux données du registre des utilisateurs.
     */
    @Autowired
    public RegistreService(RegistreDAO registreDAO) {
        this.registreDAO = registreDAO;
    }
    /**
     * Vérifie si un utilisateur existe dans le registre.
     *
     * @param username Le nom d'utilisateur à vérifier.
     * @return `true` si l'utilisateur existe, `false` sinon.
     * @throws Exception En cas d'erreur lors de la vérification.
     */
   public boolean userExists(String username) throws Exception{
        return registreDAO.userExists(username);
   }
    /**
     * Insère un nouvel utilisateur dans le registre.
     *
     * @param username Le nom d'utilisateur à insérer.
     * @param hashedPassword Le mot de passe haché de l'utilisateur.
     * @throws Exception En cas d'erreur lors de l'insertion.
     */
   public void insertUser(String username, String hashedPassword) throws Exception{
        registreDAO.insertUser(username, hashedPassword);
   }

}
