package be.helha.labos.crystalclash.DAO;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Object.BackPack;

public interface CharacterDAO {
    /**
     * @param username
     * @param characterClassName
     * */
    void saveCharacterForUser(String username, String characterClassName);
    /**
     * @param username
     * */
    String getCharacterForUser(String username);
    /**
     * @param username
     * @param characterType
     * */
    void createBackPackForCharacter(String username, String characterType);
    /**
     * @param Username
     * */
    BackPack getBackPackForCharacter(String Username);
    /**
     * @param username
     * @param backPack
     * */
    void saveBackPackForCharacter(String username, BackPack backPack);
    /**
     * @param username
     * @param name
     * @param type
     * */
    ApiReponse addObjectToBackPack(String username, String name, String type);
    /**
     * @param username
     * @param character
     * */
    void setSelectedCharacter(String username, String character);
}
