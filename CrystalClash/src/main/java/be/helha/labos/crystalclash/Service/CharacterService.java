package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Object.BackPack;
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




}
