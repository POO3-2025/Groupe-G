package be.helha.labos.crystalclash.DAO;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Object.Armor;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.Equipment;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserService;

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
    ApiReponse removeObjectFromBackPack(String username, String name);

    ApiReponse addObjectToCoffre(String username, String name, String type);

    public void setInventoryService(InventoryService inventoryService);

    ApiReponse updateReliabilityInBackPack(String username, String objectId, int newReliability);

    ApiReponse updateReliabilityInEquipment(String username, String objectId, int newReliability);

    ApiReponse deleteObjectFromBackPack(String username, String objectId);

    void createEquipmentForCharacter(String username, String characterType);

    Equipment getEquipmentForCharacter(String username);

    void saveEquipmentForCharacter(String username, Equipment equipment);

    ApiReponse addArmorToEquipment(String username, String name, String type);

    ApiReponse removeArmorFromEquipment(String username, String name);
}



