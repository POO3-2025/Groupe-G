package be.helha.labos.crystalclash.DAO;

import java.util.List;
import java.util.Map;
/**
 * Interface définissant les opérations liées à la boutique du jeu,
 * permettant de consulter les articles disponibles et d’acheter des objets.
 */
public interface ShopDAO {

    /**
     * Récupère la liste des objets disponibles à l'achat dans la boutique.
     *
     * @return une liste de mappages contenant les informations sur les objets
     *         (nom, type, prix, etc.)
     */
    List<Map<String, Object>> getShopItems();

    /**
     * Permet à un utilisateur d'acheter un objet dans la boutique.
     *
     * @param username le nom d'utilisateur qui effectue l'achat
     * @param itemName le nom de l'objet à acheter
     * @param type le type de l'objet (ex. : arme, armure, potion...)
     * @return un message ou un code indiquant le résultat de l'opération (succès, erreur, etc.)
     */
    String buyItem(String username, String itemName, String type);
}