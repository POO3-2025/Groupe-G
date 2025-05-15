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
     * @return Le personnage sélectionné.
     */
    public String getCharacterForUser(String username){
        return characterDAO.getCharacterForUser(username);
    }
    /**
     * Sauvegarde le personnage pour un utilisateur donné.
     * @param username
     * @param character
     * */
    public void saveCharacterForUser(String username, String character){
        //Logique métier pouµ la validation, gere perso, ..
        characterDAO.saveCharacterForUser(username, character);
    }
    /**
     * Crée un backpack pour un personnage donné.
     * @param username
     * @param character
     * */
    public void createBackPackForCharacter(String username, String character){
        //Logique metier pour la création de BackPack si besoin
        characterDAO.createBackPackForCharacter(username, character);
    }
    /**
     * Récupère le backpack pour un personnage donné.
     * @param username
     * */
    public BackPack getBackPackForCharacter(String username){
        return characterDAO.getBackPackForCharacter(username);
    }
    /**
     * change de  personnage sélectionné pour un utilisateur donné.
     * @param username
     * @param character
     * */
    public void setSelectedCharacter(String username, String character){
        characterDAO.setSelectedCharacter(username, character);
    }
    /**
     * Sauvegarde le backpack pour un personnage donné.
     * @param username
     * @param backPack
     * */
    public void  saveBackPackForCharacter(String username, BackPack backPack){
        if(backPack == null || backPack.getObjets().size() >5) {
            System.out.println("backpack invalide ou trop grand.");
            return;
        }
        characterDAO.saveBackPackForCharacter(username, backPack);

    }

    /**
     * Ajoute un objet au backpack d'un personnage.
     * @param username
     * @param name
     * @param type
     * */
    public ApiReponse addObjectToBackPack(String username, String name, String type){
        return characterDAO.addObjectToBackPack(username, name, type);
    }

    /**
     * Suppression d'un objet du backpack d'un personnage.
     * @param username
     * @param name
     * */
    public ApiReponse removeObjectFromBackPack(String username, String name){
        return characterDAO.removeObjectFromBackPack(username, name);
    }

    /**
     * Ajoute un objet au coffre d'un personnage.
     * @param username
     * @param name
     * @param type
     * @return
     */
    public ApiReponse addObjectToCoffre(String username, String name, String type){
        return characterDAO.addObjectToCoffre(username, name, type);
    }

    /**
     * update de la reliability d'un objet du backpack d'un personnage.
     * @param username
     * @param objectId
     * @param newReliability
     * @return
     */
    public ApiReponse updateReliabilityInBackPack(String username, String objectId, Integer newReliability) {
        return characterDAO.updateReliabilityInBackPack(username,objectId,newReliability);
    }

    /**
     * update de la reliability d'un equipement d'un personnage.
     * @param username
     * @param objectId
     * @param newReliability
     * @return
     */
    public ApiReponse updateReliabilityInEquipment(String username, String objectId, Integer newReliability) {
        return characterDAO.updateReliabilityInEquipment(username,objectId,newReliability);
    }

    /**
     * Suppression d'un objet du backpack d'un personnage.
     * @param username
     * @param objectId
     * @return
     */
    public ApiReponse deleteObjectFromBackPack(String username, String objectId) {
        return characterDAO.deleteObjectFromBackPack(username, objectId);
    }

    /**
     * creation d'un equipement pour un personnage.
     * @param username
     * @param characterType
     * @return
     */
    public void createEquipmentForCharacter(String username, String characterType){
        characterDAO.createEquipmentForCharacter(username,characterType);
    }
    /**
     * recuperer un equipement pour un personnage.
     * @param username
     * @return
     */
    public Equipment getEquipmentForCharacter(String username){
        return characterDAO.getEquipmentForCharacter(username);
    }

    /**
     * sauvegarde un equipement pour un personnage.
     * @param username
     * @param equipment
     */
    public void saveEquipmentForCharacter(String username, Equipment equipment){
        characterDAO.saveEquipmentForCharacter(username,equipment);
    }

    /**
     * Ajoute une armure à l'équipement d'un personnage.
     * @param username
     * @param name
     * @param type
     * @return
     */
    public ApiReponse addArmorToEquipment(String username, String name, String type){
        return characterDAO.addArmorToEquipment(username, name, type);
    }

    /**
     * Supprime une armure de l'équipement d'un personnage.
     * @param username
     * @param name
     * @return
     */
    public ApiReponse removeArmorFromEquipment(String username, String name){
        return characterDAO.removeArmorFromEquipment(username,name);
    }
}
