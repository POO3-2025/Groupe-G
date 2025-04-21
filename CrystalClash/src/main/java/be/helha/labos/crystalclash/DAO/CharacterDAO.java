package be.helha.labos.crystalclash.DAO;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Object.BackPack;

public interface CharacterDAO {
    void saveCharacterForUser(String username, String characterClassName);
    String getCharacterForUser(String username);
    void createBackPackForCharacter(String username, String characterType);
    BackPack getBackPackForCharacter(String Username);
    void saveBackPackForCharacter(String username, BackPack backPack);
    ApiReponse addObjectToBackPack(String username, String name, String type);
    void setSelectedCharacter(String username, String character);
}