package be.helha.labos.crystalclash.Service;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class InventoryService {
    @Autowired
    private UserService userService;
    private InventoryDAO inventoryDAO;

    /**
     * Constructeur avec injection du DAO de l'inventaire.
     * @param inventoryDAO
     * */
    @Autowired
    public InventoryService(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }
    /**
     * Crée un inventaire pour un utilisateur donné.
     * @param username
     * */
    public void createInventoryForUser(String username) {
        inventoryDAO.createInventoryForUser(username);
    }
    /**
     * Récupère l'inventaire pour un utilisateur donné.
     * @param username
     * */
    public Inventory getInventoryForUser(String username) {
        return inventoryDAO.getInventoryForUser(username);
    }
    /**
     * Sauvegarde l'inventaire pour un utilisateur donné.
     * @param username
     * @param inventory
     * */
    public void saveInventoryForUser(String username, Inventory inventory){
        inventoryDAO.saveInventoryForUser(username, inventory);
    }
    /**
     * Vente d'un objet
     * @param username
     * @param name
     * @param type
     * */
    public ApiReponse SellObject(String username, String name, String type){
        return inventoryDAO.SellObject(username, name, type);
    }
    /**
     * Ajoute un objet au coffre de l'utilisateur.
     * @param username Le nom d'utilisateur.
     * @param name Le nom de l'objet à ajouter.
     * @param type Le type de l'objet à ajouter.
     * @return Une réponse API indiquant le résultat de l'opération.
     */
    public ApiReponse addObjectToCoffre(String username, String name, String type) {
            Optional<UserInfo> userOpt = userService.getUserInfo(username);
            if (userOpt.isEmpty()) {
                return new ApiReponse("Utilisateur introuvable.", null);
            }
            // Vérifier si l'inventaire existe (en MongoDB)
            Inventory inventory = inventoryDAO.getInventoryForUser(username);
            if (inventory == null || inventory.getObjets() == null || inventory.getObjets().isEmpty()) {
                return new ApiReponse("Inventaire introuvable ou vide.", null);
            }

            // Vérifier la présence d'au moins un coffre dans l'inventaire
            boolean hasCoffre = inventory.getObjets().stream()
                    .anyMatch(obj -> obj instanceof CoffreDesJoyaux);

            if (!hasCoffre) {
                return new ApiReponse("Aucun Coffre des Joyaux trouvé dans l'inventaire.", null);
            }

            // Déléguer si tout est OK
            return inventoryDAO.addObjectToCoffre(username, name, type);
    }

    //pour test

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public CoffreDesJoyaux getCoffreDesJoyauxForUser(String username) {
        return inventoryDAO.getCoffreDesJoyauxForUser(username);
    }
}
