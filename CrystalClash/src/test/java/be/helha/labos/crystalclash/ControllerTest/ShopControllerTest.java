package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Controller.ShopController;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DAO.UserDAO;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.ShopDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserDAOImpl;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.ShopService;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.server_auth.CrystalClashApplication;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShopControllerTest {

    private ShopController shopController;
    private InventoryService inventoryService;
    private UserService userService;

    private final String TEST_USERNAME = "ShopTestUser";
    /*
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

        //Créat et vide du mongo
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", TEST_USERNAME));


        //question de facilité

        var userCombatStatDAO = new be.helha.labos.crystalclash.DAOImpl.UserCombatStatDAOImpl();
        var userCombatStatService = new be.helha.labos.crystalclash.Service.UserCombatStatService(userCombatStatDAO);
        var userDAO = new be.helha.labos.crystalclash.DAOImpl.UserDAOImpl();
        userDAO.setUserCombatStatService(userCombatStatService);

        //instance de dao manuellement, acces aux donnée
        UserDAO userDAO1 = new UserDAOImpl();//inteagit avec tb users
        InventoryDAO inventoryDAO = new InventoryDAOImpl();//gere opérations mongo

        //Pareil mais en injectant les daos (logique metier)
        //Appelle dao et validation metier
        userService = new UserService(userDAO); //recup les infos uti
        inventoryService = new InventoryService(inventoryDAO);//use mongo pour gere l inventaire
        inventoryService.setUserService(userService);

        //Prépa de shopDao
        //Logique shop
        ShopDAOImpl shopDAO = new ShopDAOImpl();//
        shopDAO.setUserService(userService); //vérif le level du user
        shopDAO.setInventoryService(inventoryService);//Verif si place et ajoute objet

        //Service et controleur manu
        //Legue logique au Dao
        ShopService shopService = new ShopService(shopDAO); //Appel controller
        shopController = new ShopController();//Ici pour les tests
        shopController.setShopService(shopService);



        inventoryDAO.createInventoryForUser(TEST_USERNAME);

        // simu user co
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TEST_USERNAME, null)
        );
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
        stmt.setString(1, TEST_USERNAME);
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.setInt(6, 0);
        stmt.setInt(7, 0);
        stmt.setInt(8, 0);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }


    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    /*
     * Test pour obtenir le shop selon le levevel du user
     * */
    @Test
    @Order(1)
    @DisplayName("Test obtenir shop")
    public void testGetShopObjets() throws Exception {
        List<Map<String, Object>> shop = shopController.getShops();
        assertFalse(shop.isEmpty(),"le shop ne devrait pas etre vide");
        assertTrue(shop.get(0).containsKey("name"));
    }


    /*
     * Test pour acheté un objet du shop
     * */
    @Test
    @Order(2)
    @DisplayName("Test achat objet")
    public void testBuyItem_success() throws Exception {

        Map<String, String> contenu = Map.of(
                "name", "Epee en bois",
                "type", "Weapon"
        );
        ResponseEntity<Map<String, Object>> response = shopController.buyItem(contenu);
        assertTrue((Boolean) response.getBody().get("success"));
        assertTrue(((String) response.getBody().get("message")).contains("acheté"));
    }

    @Test
    @Order(3)
    @DisplayName("Test achat objet avec 0 cristaux")
    public void testBuyItem_notEnoughCrystals() throws Exception {
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("UPDATE users SET cristaux = 0 WHERE username = ?");
        stmt.setString(1, TEST_USERNAME);
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        Map<String, String> payload = Map.of(
                "name", "Epee en bois",
                "type", "Weapon"
        );

        ResponseEntity<Map<String, Object>> response = shopController.buyItem(payload);
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Pas assez de cristaux !", response.getBody().get("message"));

    }

    @Test
    @Order(4)
    @DisplayName("Test achat deux fois Coffre")
    public void testBuy_Two_CoffreDesJoyaux() throws Exception {
        Map<String, String> payload = Map.of(
                "name", "Coffre des Joyaux",
                "type", "CoffreDesJoyaux"
        );

        ResponseEntity<Map<String, Object>> res1 = shopController.buyItem(payload);
        assertTrue((Boolean) res1.getBody().get("success"));

        ResponseEntity<Map<String, Object>> res2 = shopController.buyItem(payload);
        assertFalse((Boolean) res2.getBody().get("success"));
        assertEquals("Tu possèdes déjà un Coffre des Joyaux !", res2.getBody().get("message"));
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

        db.getCollection("inventory").deleteMany(new Document());
        db.getCollection("Roulette").deleteMany(new Document());
        // Ajoute d'autres collections si besoin

        System.out.println("Toutes les données Mongo ont été supprimées.");
    }

}
