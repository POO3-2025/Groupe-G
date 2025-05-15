package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Controller.UserController;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserCombatStatDAOImpl;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserCombatStatService;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.server_auth.CrystalClashApplication;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {


    private CharacterService characterService;
    private CharacterDAOImpl characterDAO;
    private InventoryService inventoryService;
    private InventoryDAOImpl inventoryDAO;
    private UserService userService;
    private UserController userController;
    private UserCombatStatService userCombatStatService;
    /**
     *¨Rediriger les co de prod vers les bases des test
     * Ici on va leurer la connection
     * c est a dire qu on va reprendre tous les parametres de la connection prod de mysql et de mongo
     * et les remplacer par ceux de la config de test
     * pour exécuter les tests dans les db appropriées
     * */
    @BeforeEach
    public void setUp() throws Exception {
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");

        var mongoProductionConfig = db.getAsJsonObject("MongoDBProduction");
        var mongoTestConfig = db.getAsJsonObject("MongoDBTest");

        //Accede aux credential de mongo
        mongoProductionConfig.getAsJsonObject("BDCredentials")
                .entrySet()//Récupe les clées, valeurs
                .forEach(entry -> { //Pour chaque clées
                    String key = entry.getKey();  //ça récup la clé (hostname,...)
                    mongoProductionConfig.getAsJsonObject("BDCredentials")
                            .add(key, mongoTestConfig.getAsJsonObject("BDCredentials").get(key)); //Et ça remplace la valeur prod pas celle de test
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

        var userDAO = new be.helha.labos.crystalclash.DAOImpl.UserDAOImpl();
        userService = new UserService(userDAO);

        inventoryDAO = new InventoryDAOImpl();
        inventoryDAO.setUserService(userService);

        inventoryService = new InventoryService(inventoryDAO);
        inventoryService.setUserService(userService);

        characterDAO = new CharacterDAOImpl();
        characterDAO.setInventoryService(inventoryService);

        characterService = new CharacterService(characterDAO);

        userController = new UserController();
        userController.setUserService(userService);
        userController.setCharacterService(characterService);

        userCombatStatService = new UserCombatStatService(new UserCombatStatDAOImpl());
        userController.setUserCombatStatService(userCombatStatService);

    }

    @BeforeEach
    public void ensureUserExists_and_mongo() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive)
            VALUES (?, ?, ?, ?, ?, ?, ?,?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "UserControllerTestUser");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.setInt(6, 0); // valeur par défaut pour 'gagner'
        stmt.setInt(7, 0); // valeur par défaut pour 'perdu'
        stmt.setInt(8, 0);
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        //Insertion pour le perso du user .
        MongoDatabase mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").insertOne(new Document("username", "UserControllerTestUser")
                .append("type", "troll")
                .append("selected", true));

    }

    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    /**
    * Test recup le user.
    * */
    @Test
    @Order(1)
    @DisplayName("Test récupération d’un utilisateur existant")
    public void RecupUserExistant() {
        String username = "UserControllerTestUser";

        // Appel direct de la méthode du contrôleur
        ResponseEntity<?> response = userController.getUserByUsername(username);

        Assertions.assertEquals(200, response.getStatusCodeValue());

        Object body = response.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertInstanceOf(UserInfo.class, body);

        UserInfo userInfo = (UserInfo) body;
        Assertions.assertEquals(username, userInfo.getUsername());
        Assertions.assertEquals(5, userInfo.getLevel());
        Assertions.assertEquals(100, userInfo.getCristaux());
        Assertions.assertEquals(0, userInfo.getGagner());
        Assertions.assertEquals(0, userInfo.getPerdu());
        Assertions.assertEquals("troll", userInfo.getSelectedCharacter());
    }


    /**
     * Test si un user est bien inexistant
     * */
    @Test
    @Order(2)
    @DisplayName("Test récupération d’un utilisateur inexistant")
    public void testGetUserByUsername_notFound() {
        String username = "InexistantUser";

        ResponseEntity<?> response = userController.getUserByUsername(username);

        Assertions.assertEquals(404, response.getStatusCodeValue());

        Object body = response.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertInstanceOf(Map.class, body);

        Map<String, String> errorResponse = (Map<String, String>) body;
        Assertions.assertEquals("Utilisateur introuvable", errorResponse.get("message"));
    }

      /**
       * test recup stat depuis mongo
       * **/
        @Test
        @Order(3)
        @DisplayName("Test récupération stat dans mongo")
        public void testGetStat() {
            String username = "mysqltest";

            userCombatStatService.createStatsForUser(username);

            ResponseEntity<?> response = userController.getStats(username);

            Assertions.assertEquals(200, response.getStatusCodeValue());

            assertNotNull(response.getBody());
        }


    @AfterAll
    public void resetMySQLUsers_AND_Mongo() throws Exception {
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("DELETE FROM users");
        stmt.executeUpdate();
        stmt.close();
        conn.close();
        System.out.println("Tous les utilisateurs MySQL ont été supprimés.");

        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        db.getCollection("Inventory").deleteMany(new Document());
        db.getCollection("Characters").deleteMany(new Document());
        // Ajoute d'autres collections si besoin

        System.out.println("Toutes les données Mongo ont été supprimées.");
    }



}
