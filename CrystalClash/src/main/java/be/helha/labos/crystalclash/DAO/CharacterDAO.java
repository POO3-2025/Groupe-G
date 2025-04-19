package be.helha.labos.crystalclash.DAO;


import be.helha.labos.crystalclash.Object.BackPack;

public interface CharacterDAO {
    void saveCharacterForUser(String username, String characterClassName);
    String getCharacterForUser(String username);
    void createBackPackForCharacter(String username, String characterType);
    BackPack getBackPackForCharacter(String Username);

}