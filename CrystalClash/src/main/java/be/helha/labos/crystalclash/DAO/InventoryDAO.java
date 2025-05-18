package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;

/**
 * Interface définissant les opérations d'accès aux données
 * liées à l'inventaire des utilisateurs dans le jeu.
 * Elle permet la gestion du sac d'objets, des coffres et de la vente d’objets.
 */
public interface InventoryDAO {

    /**
     * Récupère l'inventaire d'un utilisateur donné.
     *
     * @param username le nom d'utilisateur
     * @return l'inventaire associé à cet utilisateur
     */
    Inventory getInventoryForUser(String username);

    /**
     * Crée un inventaire vide pour un utilisateur donné.
     *
     * @param username le nom d'utilisateur
     */
    void createInventoryForUser(String username);

    /**
     * Sauvegarde l'inventaire d'un utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param inventory l'objet Inventory à sauvegarder
     */
    void saveInventoryForUser(String username, Inventory inventory);

    /**
     * Permet à l'utilisateur de vendre un objet de son inventaire.
     *
     * @param username le nom d'utilisateur
     * @param name le nom de l'objet à vendre
     * @param type le type de l'objet (ex : arme, potion, etc.)
     * @return une réponse contenant le statut de l'opération de vente
     */
    ApiReponse SellObject(String username, String name, String type);

    /**
     * Ajoute un objet dans le coffre de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param name le nom de l'objet à ajouter
     * @param type le type de l'objet
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse addObjectToCoffre(String username, String name, String type);

    /**
     * Récupère le coffre à joyaux d’un utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return l'objet {@link CoffreDesJoyaux} associé à cet utilisateur
     */
    CoffreDesJoyaux getCoffreDesJoyauxForUser(String username);
}
