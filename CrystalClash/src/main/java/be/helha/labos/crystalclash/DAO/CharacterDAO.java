package be.helha.labos.crystalclash.DAO;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Object.Armor;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.Equipment;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserService;

/**
 * Interface définissant les opérations liées à la gestion des personnages,
 * des sacs à dos (backpack) et de l’équipement pour un utilisateur.
 * Elle sert d’abstraction pour la couche de persistance.
 */
public interface CharacterDAO {

    /**
     * Sauvegarde un personnage sélectionné pour un utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param characterClassName le nom de la classe du personnage choisi
     */
    void saveCharacterForUser(String username, String characterClassName);

    /**
     * Récupère le personnage sélectionné pour un utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return le nom de la classe du personnage
     */
    String getCharacterForUser(String username);

    /**
     * Crée un sac à dos vide pour un personnage spécifique d'un utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param characterType le type du personnage
     */
    void createBackPackForCharacter(String username, String characterType);

    /**
     * Récupère le sac à dos associé au personnage de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return le sac à dos (BackPack)
     */
    BackPack getBackPackForCharacter(String username);

    /**
     * Sauvegarde l'état actuel du sac à dos pour le personnage de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param backPack l'objet BackPack à sauvegarder
     */
    void saveBackPackForCharacter(String username, BackPack backPack);

    /**
     * Ajoute un objet au sac à dos de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param name le nom de l'objet
     * @param type le type de l'objet (ex : arme, potion, etc.)
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse addObjectToBackPack(String username, String name, String type);

    /**
     * Définit le personnage actuellement sélectionné par l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param character le nom du personnage
     */
    void setSelectedCharacter(String username, String character);

    /**
     * Supprime un objet du sac à dos d’un utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param name le nom de l'objet à supprimer
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse removeObjectFromBackPack(String username, String name);

    /**
     * Ajoute un objet au coffre de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param name le nom de l'objet
     * @param type le type de l'objet
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse addObjectToCoffre(String username, String name, String type);

    /**
     * Injecte le service d'inventaire dans cette DAO.
     *
     * @param inventoryService l'instance du service d'inventaire
     */
    void setInventoryService(InventoryService inventoryService);

    /**
     * Met à jour la fiabilité (durabilité) d’un objet dans le sac à dos.
     *
     * @param username le nom d'utilisateur
     * @param objectId l'identifiant de l'objet
     * @param newReliability la nouvelle valeur de fiabilité
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse updateReliabilityInBackPack(String username, String objectId, int newReliability);

    /**
     * Met à jour la fiabilité (durabilité) d’un objet dans l’équipement.
     *
     * @param username le nom d'utilisateur
     * @param objectId l'identifiant de l'objet
     * @param newReliability la nouvelle valeur de fiabilité
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse updateReliabilityInEquipment(String username, String objectId, int newReliability);

    /**
     * Supprime un objet du sac à dos selon son identifiant.
     *
     * @param username le nom d'utilisateur
     * @param objectId l'identifiant de l'objet
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse deleteObjectFromBackPack(String username, String objectId);

    /**
     * Crée un équipement vide pour un personnage.
     *
     * @param username le nom d'utilisateur
     * @param characterType le type de personnage
     */
    void createEquipmentForCharacter(String username, String characterType);

    /**
     * Récupère l’équipement associé au personnage de l’utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return l'objet Equipment correspondant
     */
    Equipment getEquipmentForCharacter(String username);

    /**
     * Sauvegarde l’équipement du personnage.
     *
     * @param username le nom d'utilisateur
     * @param equipment l'objet Equipment à sauvegarder
     */
    void saveEquipmentForCharacter(String username, Equipment equipment);

    /**
     * Ajoute une armure à l’équipement du personnage.
     *
     * @param username le nom d'utilisateur
     * @param name le nom de l'armure
     * @param type le type de l'armure
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse addArmorToEquipment(String username, String name, String type);

    /**
     * Retire une armure de l’équipement du personnage.
     *
     * @param username le nom d'utilisateur
     * @param name le nom de l'armure à retirer
     * @return une réponse contenant le statut de l'opération
     */
    ApiReponse removeArmorFromEquipment(String username, String name);
}