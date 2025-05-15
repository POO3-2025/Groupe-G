package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.ShopDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShopService {

    private final ShopDAO shopDAO;


    /**
     * Constructeur avec injection du DAO de la boutique.
     *
     * @param shopDAO DAO pour accéder aux données de la boutique.
     */
    @Autowired
    public ShopService(ShopDAO shopDAO) {
        this.shopDAO = shopDAO;
    }

    /**
     * Récupère la liste des articles disponibles dans la boutique.
     *
     * @return Une liste de maps contenant les informations des articles de la boutique.
     */
    public List<Map<String, Object>> getShopItems(){
        return shopDAO.getShopItems();
    }
    /**
     * Permet à un utilisateur d'acheter un article dans la boutique.
     *
     * @param username Le nom d'utilisateur de l'acheteur.
     * @param itemName Le nom de l'article à acheter.
     * @param type Le type de l'article.
     * @return Une chaîne indiquant le résultat de l'achat.
     */
    public String  buyItem(String username, String itemName, String type){
        return shopDAO.buyItem(username, itemName, type);
    }


}
