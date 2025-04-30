package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.Controller.InventoryController;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;

//Simulation d'un user connécter pour les tests qui en ont besoin
import be.helha.labos.crystalclash.Service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryControllerTest {  // <- ici "Test" !

    private InventoryController inventoryController;
    private InventoryService inventoryService;
    private InventoryDAOImpl inventoryDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        //Forcer l'utilisation de mongo
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");
        var mongoProductionConfig = db.getAsJsonObject("MongoDBProduction");
        var mongoTestConfig = db.getAsJsonObject("MongoDBTest");

        //contient la config de prod  et mongoTestConfig celle de test
        //C'est un style de leure, ça va copié les params de de connexion de mongoTest vers Production
        //Le config pense utiliser la db de prod hors que non
        mongoProductionConfig.getAsJsonObject("BDCredentials")
            .entrySet()
            .forEach(entry -> {
                String key = entry.getKey();
                mongoProductionConfig.getAsJsonObject("BDCredentials")
                    .add(key, mongoTestConfig.getAsJsonObject("BDCredentials").get(key));

            });
        var mysqlProductionConfig = db.getAsJsonObject("mysqlproduction");
        var mysqlTestConfig = db.getAsJsonObject("mysqltest");
        mysqlProductionConfig.getAsJsonObject("BDCredentials")
            .entrySet()
            .forEach(entry -> {
                String key = entry.getKey();
                mysqlProductionConfig.getAsJsonObject("BDCredentials")
                    .add(key, mysqlTestConfig.getAsJsonObject("BDCredentials").get(key));
            });


        //Instancier sois même
        inventoryDAO = new InventoryDAOImpl();
        inventoryService = new InventoryService(inventoryDAO);

        inventoryController = new InventoryController();
        inventoryController.setInventoryService(inventoryService);

        //Création manuelle de dao user
        //Ensuite on l'injecte dans le service
        //puis on le lie au daoImpl
        var userDAO = new be.helha.labos.crystalclash.DAOImpl.UserDAOImpl();
        var userService = new UserService(userDAO);
        inventoryDAO.setUserService(userService);
    }


    /**
     * Affiche le nom du test en cours d'exécution.
     *
     * @param testInfo Informations sur le test en cours.
     */
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @BeforeEach
    public void ensureUserExists() {
        try {
            var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
            var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux)
        """);
            stmt.setString(1, "inventoryTestUser");
            stmt.setString(2, "password");   // mot de passe bidon pour test
            stmt.setInt(3, 1);               // niveau de départ
            stmt.setInt(4, 100);            // cristaux de départ
            stmt.setBoolean(5, false);      // connecté ou non
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Impossible d'insérer l'utilisateur de test en base MySQL");
        }
    }


    @Test
    @Order(1)
    @DisplayName("Test avoir inventaire d'un user")
    public void testGetInventory_existingUser() {
        // Arrange
        String username = "inventoryTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        inventoryDAO.createInventoryForUser(username);

        // Appelle de la méthode dans le controller
        Inventory inventory = inventoryController.getInventory(username);

        // Assert
        assertNotNull(inventory, "Inventory ne doit pas être null");
        assertEquals(0, inventory.getObjets().size(), "L'inventaire nouvellement créé doit être vide");
    }

    @Test
    @Order(2)
    @DisplayName("Test vendre un objet qui est dans l'inventaire")
    public void testSellObject_success() {
        String username = "inventoryTestUser";

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Nettoyer Mongo
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Ajouter un objet dans l'inventaire
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée de test", 100,5,5,5);
        objet.setType("Weapon");
        inventory.ajouterObjet(objet);

        // Sauvegarder l'inventaire
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Construire le payload comme dans un vrai appel POST dans le construct
        Map<String, String> payload = Map.of(
            "name", "Épée de test",
            "type", "Weapon"
        );

        // Appeler le controller
        var response = inventoryController.sellObject(payload);
        System.out.println("Réponse du contrôleur : " + response.getBody().getMessage());
        // vérif la réponse
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Objet vendu avec succès"));

        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) response.getBody().getData();

        assertEquals(50.0, ((Number) dataMap.get("gain")).doubleValue());
        assertEquals("medium", dataMap.get("rarity")); // car prix = 100
    }

    @Test
    @Order(3)
    @DisplayName("Test vendre un objet qui n'est pas dans l'inventaire")
    public void testSellObject_objectNotFound() {
        String username = "inventoryTestUser";

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        // Créer un inventaire vide
        inventoryDAO.createInventoryForUser(username);

        // Appeler le contrôleur avec un objet qui n'existe pas
        Map<String, String> payload = Map.of(
            "name", "ObjetInexistant",
            "type", "Weapon"
        );

        var response = inventoryController.sellObject(payload);
        System.out.println("Réponse (objet manquant) : " + response.getBody().getMessage());

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Objet non trouvé dans l'inventaire.", response.getBody().getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("Test vendre un objet avec utilisateur inexistant")
    public void testSellObject_userNotFound() {
        String username = "userInexistant";

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Supprimer l’utilisateur s’il existe
        try {
            var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
            var stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Impossible de supprimer l'utilisateur de test");
        }

        // Créer inventaire avec l’objet
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));
        inventoryDAO.createInventoryForUser(username);

        var inventory = inventoryDAO.getInventoryForUser(username);
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée de test", 100, 5, 5, 5);
        objet.setType("Weapon");
        inventory.ajouterObjet(objet);
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Appeler avec un vrai objet mais faux user
        Map<String, String> payload = Map.of(
            "name", "Épée de test",
            "type", "Weapon"
        );

        var response = inventoryController.sellObject(payload);
        System.out.println("Réponse (user inexistant) : " + response.getBody().getMessage());

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Utilisateur introuvable.", response.getBody().getMessage());
    }


    /**Test Estelle*/

    @AfterEach
    public void cleanUp() {
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", "inventoryTestUser"));
    }

}
