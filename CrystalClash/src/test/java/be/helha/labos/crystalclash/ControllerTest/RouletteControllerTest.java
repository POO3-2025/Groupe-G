package be.helha.labos.crystalclash.ControllerTest;


import be.helha.labos.crystalclash.Controller.InventoryController;
import be.helha.labos.crystalclash.Controller.RouletteController;
import be.helha.labos.crystalclash.DAO.RouletteDAO;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.RouletteDAOImpl;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.RouletteService;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.server_auth.*;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static com.mongodb.client.model.Filters.eq;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RouletteControllerTest {


    private RouletteController rouletteController;
    public UserService userService;
    public RouletteDAO rouletteDAO;
    public RouletteService rouletteService;

    private InventoryService inventoryService;
    private InventoryDAOImpl inventoryDAO;
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

        var userDAO = new be.helha.labos.crystalclash.DAOImpl.UserDAOImpl();
        userService = new UserService(userDAO);

        inventoryDAO = new InventoryDAOImpl();
        inventoryDAO.setUserService(userService);

        inventoryService = new InventoryService(inventoryDAO);
        inventoryService.setUserService(userService);


        rouletteDAO = new RouletteDAOImpl();

        rouletteService = new RouletteService();
        rouletteService.setRouletteDAO(rouletteDAO);
        rouletteService.setUserService(userService);
        rouletteService.setInventoryService(inventoryService);

        rouletteController = new RouletteController();
        rouletteController.setRouletteService(rouletteService);
    }
    //Clean la roulette avant pour certains tests
    @BeforeEach
    public void cleanupMongoRoulette() {
        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        db.getCollection("Roulette").deleteMany(new Document()); // supprime tout
    }

/*
* Crée le user dans mysql et vider son inventaire
* */
    @BeforeEach
    public void ensureUserExists() {
        try {
            var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
            var stmt = conn.prepareStatement("""
                INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux)
            """);
            stmt.setString(1, "RouletteTestUser10" +
                    "");
            stmt.setString(2, "password");
            stmt.setInt(3, 1);
            stmt.setInt(4, 100);
            stmt.setBoolean(5, false);
            stmt.setInt(6, 0); // valeur par défaut pour 'gagner'
            stmt.setInt(7, 0); // valeur par défaut pour 'perdu'
            stmt.setInt(8, 0);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Impossible d'insérer l'utilisateur de test en base MySQL");
        }

    }

    /*
     *Crée un inventaire au user avec les tests
     * */
    @BeforeEach
    public void EnsureInventoryMongo() throws Exception {
        // Crée un inventaire
        // Créer inventaire avec l’objet
        String username = "RouletteTestUser10";
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));


    }


    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    /*
    * Test si le joeur peut bien jouer a la roule est que tout ce passe bien
    * */
    @Test
    @Order(1)
    @DisplayName("Test jouer a la roulette OK")
    public void testPlayRoulette_success() throws Exception {
        // Créer un utilisateur de test (tu peux aussi le créer dans ta DB avec JDBC comme tu le fais déjà)
        String username = "RouletteTestUser10";

        // Appel direct
        ObjectBase result = rouletteService.PlayRoulette(username);

        inventoryService.createInventoryForUser(username);
        // Vérifications
        assertNotNull(result);
        System.out.println("Objet obtenu : " + result.getName() + " (" + result.getType() + ")");
    }

    /*
    * Voir si l'inventaire du user est bien mis a jout avec l'objet gagné dedans.
    * Pour cela on joue a la roulette
    *on récup l'invenataire du user
    * en cherchant avec son username
    *
    * */
    @Test
    @Order(2)
    @DisplayName("Test inventaire update avec objet gagné")
    @WithMockUser(username = "RouletteTestUser10")
    public void testPlayRoulette_inventoryUpdatedInMongo() throws Exception {
        // Créer un utilisateur de test (tu peux aussi le créer dans ta DB avec JDBC comme tu le fais déjà)
        String username = "RouletteTestUser10";

        inventoryService.createInventoryForUser(username);

        // Appel direct
        ObjectBase result = rouletteService.PlayRoulette(username);

        Inventory inventory = inventoryService.getInventoryForUser(username);


        List<ObjectBase> objets = inventory.getObjets();

        boolean found = objets.stream().anyMatch(o ->
                o.getName().equals(result.getName()) &&
                        o.getType().equals(result.getType()) &&
                        o.getPrice() == result.getPrice()
        );
        assertTrue(found);
    }

    /*
    * Test pour voir si le joeur peut bien joué qu'une fois par jour
    * on appelle pour cela deux fois la requete
    * */
    @Test
    @Order(3)
    @DisplayName("Test si user peut ne bien joué qu une fois")
    public void testPlayRoulette_onlyOncePerDay() throws Exception {
        String username = "RouletteTestUser10";

        ObjectBase result = rouletteService.PlayRoulette(username);
        assertNotNull(result, "Le premier tirage doit réussir");

        // Deuxième appel : doit échouer
        Exception exception = assertThrows(Exception.class, () -> {
            rouletteService.PlayRoulette(username);
        });

        assertTrue(exception.getMessage().contains("Déjà joué aujourd'hui"),
                "Le deuxième tirage doit échouer avec le bon message");

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
