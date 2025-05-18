package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Service pour gérer les opérations liées aux personnages, aux équipements et aux backpacks.
 * Fournit une couche métier entre le contrôleur et le DAO.
 */
@Service
public class CharacterService {

    private final CharacterDAO characterDAO;

    /**
     * Constructeur avec injection du DAO des personnages.
     *
     * @param characterDAO DAO pour accéder aux données des personnages.
     */
    @Autowired
    public CharacterService(CharacterDAO characterDAO) {
        this.characterDAO = characterDAO;
    }

    /**
     * Récupère le personnage sélectionné pour un utilisateur donné.
     *
     * @param username Le nom d'utilisateur.
     * @return Le nom du personnage sélectionné.
     */
    public String getCharacterForUser(String username){
        return characterDAO.getCharacterForUser(username);
    }

    /**
     * Sauvegarde le personnage sélectionné pour un utilisateur donné.
     *
     * @param username Nom d'utilisateur.
     * @param character Nom du personnage à sauvegarder.
     */
    public void saveCharacterForUser(String username, String character){
        // Logique métier pour la validation, gestion du personnage, etc.
        characterDAO.saveCharacterForUser(username, character);
    }

    /**
     * Crée un backpack pour un personnage donné.
     *
     * @param username Nom d'utilisateur.
     * @param character Nom du personnage.
     */
    public void createBackPackForCharacter(String username, String character){
        // Logique métier pour la création du backpack si nécessaire.
        characterDAO.createBackPackForCharacter(username, character);
    }

    /**
     * Récupère le backpack associé au personnage de l'utilisateur.
     *
     * @param username Nom d'utilisateur.
     * @return Le backpack du personnage.
     */
    public BackPack getBackPackForCharacter(String username){
        return characterDAO.getBackPackForCharacter(username);
    }

    /**
     * Définit le personnage sélectionné pour un utilisateur.
     *
     * @param username Nom d'utilisateur.
     * @param character Nom du personnage à sélectionner.
     */
    public void setSelectedCharacter(String username, String character){
        characterDAO.setSelectedCharacter(username, character);
    }

    /**
     * Sauvegarde le contenu du backpack pour un personnage donné.
     *
     * @param username Nom d'utilisateur.
     * @param backPack Backpack à sauvegarder.
     */
    public void saveBackPackForCharacter(String username, BackPack backPack){
        if (backPack == null || backPack.getObjets().size() > 5) {
            System.out.println("backpack invalide ou trop grand.");
            return;
        }
        characterDAO.saveBackPackForCharacter(username, backPack);
    }

    /**
     * Ajoute un objet au backpack d'un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param name Nom de l'objet.
     * @param type Type de l'objet.
     * @return Réponse API avec le résultat de l'opération.
     */
    public ApiReponse addObjectToBackPack(String username, String name, String type){
        return characterDAO.addObjectToBackPack(username, name, type);
    }

    /**
     * Supprime un objet du backpack d'un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param name Nom de l'objet à supprimer.
     * @return Réponse API avec le résultat de l'opération.
     */
    public ApiReponse removeObjectFromBackPack(String username, String name){
        return characterDAO.removeObjectFromBackPack(username, name);
    }

    /**
     * Ajoute un objet au coffre (inventaire étendu) d'un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param name Nom de l'objet.
     * @param type Type de l'objet.
     * @return Réponse API avec le résultat de l'opération.
     */
    public ApiReponse addObjectToCoffre(String username, String name, String type){
        return characterDAO.addObjectToCoffre(username, name, type);
    }

    /**
     * Met à jour la fiabilité (reliability) d’un objet dans le backpack d’un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param objectId Identifiant de l'objet.
     * @param newReliability Nouvelle valeur de fiabilité.
     * @return Réponse API avec le résultat de l'opération.
     */
    public ApiReponse updateReliabilityInBackPack(String username, String objectId, Integer newReliability) {
        return characterDAO.updateReliabilityInBackPack(username, objectId, newReliability);
    }

    /**
     * Met à jour la fiabilité (reliability) d’un objet dans l’équipement d’un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param objectId Identifiant de l'objet.
     * @param newReliability Nouvelle valeur de fiabilité.
     * @return Réponse API avec le résultat de l'opération.
     */
    public ApiReponse updateReliabilityInEquipment(String username, String objectId, Integer newReliability) {
        return characterDAO.updateReliabilityInEquipment(username, objectId, newReliability);
    }

    /**
     * Supprime un objet du backpack d’un personnage via son identifiant.
     *
     * @param username Nom d'utilisateur.
     * @param objectId Identifiant de l’objet à supprimer.
     * @return Réponse API avec le résultat de l’opération.
     */
    public ApiReponse deleteObjectFromBackPack(String username, String objectId) {
        return characterDAO.deleteObjectFromBackPack(username, objectId);
    }

    /**
     * Crée un équipement de base pour un personnage donné.
     *
     * @param username Nom d'utilisateur.
     * @param characterType Type de personnage.
     */
    public void createEquipmentForCharacter(String username, String characterType){
        characterDAO.createEquipmentForCharacter(username, characterType);
    }

    /**
     * Récupère l'équipement associé à un personnage.
     *
     * @param username Nom d'utilisateur.
     * @return L'équipement du personnage.
     */
    public Equipment getEquipmentForCharacter(String username){
        return characterDAO.getEquipmentForCharacter(username);
    }

    /**
     * Sauvegarde l'équipement d'un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param equipment Équipement à sauvegarder.
     */
    public void saveEquipmentForCharacter(String username, Equipment equipment){
        characterDAO.saveEquipmentForCharacter(username, equipment);
    }

    /**
     * Ajoute une armure à l'équipement d'un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param name Nom de l'armure.
     * @param type Type de l'armure.
     * @return Réponse API avec le résultat de l'opération.
     */
    public ApiReponse addArmorToEquipment(String username, String name, String type){
        return characterDAO.addArmorToEquipment(username, name, type);
    }

    /**
     * Supprime une armure de l'équipement d'un personnage.
     *
     * @param username Nom d'utilisateur.
     * @param name Nom de l'armure à retirer.
     * @return Réponse API avec le résultat de l'opération.
     */
    public ApiReponse removeArmorFromEquipment(String username, String name){
        return characterDAO.removeArmorFromEquipment(username, name);
    }
}

