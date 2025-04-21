package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Object.BackPack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CharacterService {

    private final CharacterDAO characterDAO;

    @Autowired
    public CharacterService(CharacterDAO characterDAO) {
        this.characterDAO = characterDAO;
    }

    public String getCharacterForUser(String username){
        return characterDAO.getCharacterForUser(username);
    }

    public void saveCharacterForUser(String username, String character){
        //Logique métier pour la validation, gere perso, ..
        characterDAO.saveCharacterForUser(username, character);
    }

    public void createBackPackForCharacter(String username, String character){
        //Logique metier pour la création de BackPack si besoin
        characterDAO.createBackPackForCharacter(username, character);
    }

    public BackPack getBackPackForCharacter(String username){
        return characterDAO.getBackPackForCharacter(username);
    }

    public void setSelectedCharacter(String username, String character){
        characterDAO.setSelectedCharacter(username, character);
    }
    public void  saveBackPackForCharacter(String username, BackPack backPack){
        characterDAO.saveBackPackForCharacter(username, backPack);
    }

    public ApiReponse addObjectToBackPack(String username, String name, String type){
        return characterDAO.addObjectToBackPack(username, name, type);
    }


}