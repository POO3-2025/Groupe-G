package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CharacterService {

    private final CharacterDAO characterDAO;

    /**
     * @param characterDAO
     * */
    @Autowired
    public CharacterService(CharacterDAO characterDAO) {
        this.characterDAO = characterDAO;
    }
    /**
     * @param username
     * */
    public String getCharacterForUser(String username){
        return characterDAO.getCharacterForUser(username);
    }
    /**
     * @param username
     * @param character
     * */
    public void saveCharacterForUser(String username, String character){
        //Logique métier pouµ la validation, gere perso, ..
        characterDAO.saveCharacterForUser(username, character);
    }
    /**
     * @param username
     * @param character
     * */
    public void createBackPackForCharacter(String username, String character){
        //Logique metier pour la création de BackPack si besoin
        characterDAO.createBackPackForCharacter(username, character);
    }
    /**
     * @param username
     * */
    public BackPack getBackPackForCharacter(String username){
        return characterDAO.getBackPackForCharacter(username);
    }
    /**
     * @param username
     * @param character
     * */
    public void setSelectedCharacter(String username, String character){
        characterDAO.setSelectedCharacter(username, character);
    }
    /**
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
     * @param username
     * @param name
     * @param type
     * */
    public ApiReponse addObjectToBackPack(String username, String name, String type){
        return characterDAO.addObjectToBackPack(username, name, type);
    }

    public ApiReponse removeObjectFromBackPack(String username, String name){
        return characterDAO.removeObjectFromBackPack(username, name);
    }

    public ApiReponse addObjectToCoffre(String username, String name, String type){
        return characterDAO.addObjectToCoffre(username, name, type);
    }

    public ApiReponse updateReliabilityInBackPack(String username, String objectId, Integer newReliability) {
        return characterDAO.updateReliabilityInBackPack(username,objectId,newReliability);
    }

    public ApiReponse updateReliabilityInEquipment(String username, String objectId, Integer newReliability) {
        return characterDAO.updateReliabilityInEquipment(username,objectId,newReliability);
    }

    public ApiReponse deleteObjectFromBackPack(String username, String objectId) {
        return characterDAO.deleteObjectFromBackPack(username, objectId);
    }

    public void createEquipmentForCharacter(String username, String characterType){
        characterDAO.createEquipmentForCharacter(username,characterType);
    }

    public Equipment getEquipmentForCharacter(String username){
        return characterDAO.getEquipmentForCharacter(username);
    }

    public void saveEquipmentForCharacter(String username, Equipment equipment){
        characterDAO.saveEquipmentForCharacter(username,equipment);
    }

    public ApiReponse addArmorToEquipment(String username, String name, String type){
        return characterDAO.addArmorToEquipment(username, name, type);
    }

    public ApiReponse removeArmorFromEquipment(String username, String name){
        return characterDAO.removeArmorFromEquipment(username,name);
    }
}
