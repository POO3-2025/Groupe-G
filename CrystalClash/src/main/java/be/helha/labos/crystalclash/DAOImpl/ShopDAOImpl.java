package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import be.helha.labos.crystalclash.DAO.ShopDAO;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Factory.ObjectFactory;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.*;
@Repository
public class ShopDAOImpl implements ShopDAO{
    @Autowired
    private UserService userService;

    @Autowired
    private InventoryService inventoryService;

    /*
     *Grace a la méthode dans ObjectFactory qui est getAllObjectsByName ou va aller rechercher les objects dispo
     * Retourne tous les objest dispo dans la Map (afit comme un catalogue)
     *Chaque Map represente un objet de la boutique
     */
    @Override
    public List<Map<String, Object>> getShopItems() {

        //liste vide pour la remplir avec une Map
        List<Map<String, Object>> shopItems = new ArrayList<>();

        //Boucle sur les objets dispo (nom de l'objet et l'objet lui meme)
        for (Map.Entry<String, ObjectBase> entry : ObjectFactory.getAllObjectsByName().entrySet()){
            ObjectBase item = entry.getValue();
            //pour chauque objet ben on crée une Map... (Sert a formater l'objet de facon lisible pour l'api, spring peut la convertire plus facilement)
            Map<String, Object> shopItem = new HashMap<>();
            shopItem.put("type", item.getClass().getSimpleName());
            shopItem.put("name", item.getName());
            shopItem.put("price", item.getPrice());
            shopItem.put("requiredLevel", item.getRequiredLevel());

            shopItems.add(shopItem);

        }
        //Tri objet par niveau, plus beosin je pense mais je garde on ne sais jamais
        shopItems.sort(Comparator.comparingInt(i -> (int) i.get("requiredLevel")));
        return shopItems;
    }

    /**
     * @param username
     * @param itemName
     * @param type
     * permet d'acheter un objet dans le shop
     * il faut récup les infos du joueur avant pour savoir sont level et affiher ce qu il faut dans le shop
     * */
    @Override
    public String  buyItem(String username, String itemName, String type) {
        try {
            // Récupère les infos du joueur (niveau et cristaux)
            Optional<UserInfo> userOpt = userService.getUserInfo(username);
            if (userOpt.isEmpty()) {
                return "Utilisateur introuvable.";
            }

            //Extration du niveau et cristaux, serivira pour voir  si le joueur pourra acheter l'objet et faire la soustraction des cristaux
            UserInfo user = userOpt.get();
            int levelPlayer = user.getLevel();
            int playerCrystals = user.getCristaux();

            // Crée l'objet (avec vérification de niveau)
            ObjectBase item = ObjectFactory.CreateObject(itemName, type, levelPlayer);
            System.out.println("DEBUG : " + item.getName() + " / type = " + item.getType());

            // Récupère l'inventaire du joueur
            Inventory inventory = inventoryService.getInventoryForUser(username);

            // Vérifie si déjà possédé
            if (item.getName().equalsIgnoreCase("Coffre des Joyaux")) {
                boolean alreadyOwned = inventory.getObjets().stream()
                        .anyMatch(obj -> obj.getName().equalsIgnoreCase("Coffre des Joyaux"));
                if (alreadyOwned) return "Tu possèdes déjà un Coffre des Joyaux !";
            }


            // Vérifie si déjà possédé
            if (item.getName().equalsIgnoreCase("Coffre des Joyaux")) {
                boolean alreadyOwned = inventory.getObjets().stream()
                    .anyMatch(obj -> obj.getName().equalsIgnoreCase("Coffre des Joyaux"));
                if (alreadyOwned) return "Tu possèdes déjà un Coffre des Joyaux !";
            }

            if (inventory.getObjets().size() >= 30) {
                return "Inventaire plein !";

            }

            if (playerCrystals < item.getPrice()) {
                return "Pas assez de cristaux !";
            }



            // Achat validé
            inventory.ajouterObjet(item);
            inventoryService.saveInventoryForUser(username, inventory);
            userService.updateCristaux(username, playerCrystals - item.getPrice());

            return item.getName() + " acheté avec succès !";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur inattendue lors de l'achat.";
        }
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
}
