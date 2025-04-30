package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.Controller.InventoryController;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryController {

    private InventoryController inventoryController;
    private InventoryService inventoryService;
    private InventoryDAOImpl inventoryDAO;

    @BeforeEach
    public void setUp() throws Exception {
        // ⚡ Forcer MongoDBProduction -> MongoDBTest
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");
        var mongoProductionConfig = db.getAsJsonObject("MongoDBProduction");
        var mongoTestConfig = db.getAsJsonObject("MongoDBTest");

        mongoProductionConfig.getAsJsonObject("BDCredentials")
            .entrySet().forEach(entry -> {
                String key = entry.getKey();
                mongoProductionConfig.getAsJsonObject("BDCredentials")
                    .add(key, mongoTestConfig.getAsJsonObject("BDCredentials").get(key));
            });

        inventoryDAO = new InventoryDAOImpl(); // se connecte à MongoDBTest
        inventoryService = new InventoryService(inventoryDAO);

        inventoryController = new InventoryController();
        inventoryController.setInventoryService(inventoryService); // Setter à ajouter dans InventoryController
    }

    @Test
    @Order(1)
    public void testGetInventory_existingUser() {
        // Arrange
        String username = "inventoryTestUser";

        // Supprime toute trace précédente pour être propre
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        // Crée un inventaire vide pour ce user
        inventoryDAO.createInventoryForUser(username);

        // Act
        Inventory inventory = inventoryController.getInventory(username);

        // Assert
        assertNotNull(inventory, "Inventory ne doit pas être null");
        assertEquals(0, inventory.getObjets().size(), "L'inventaire nouvellement créé doit être vide");
    }

    @AfterEach
    public void cleanUp() {
        // Supprime l'inventaire test après chaque test
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", "inventoryTestUser"));
    }
}
