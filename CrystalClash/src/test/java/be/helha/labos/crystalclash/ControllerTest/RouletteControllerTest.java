package be.helha.labos.crystalclash.ControllerTest;


import be.helha.labos.crystalclash.Controller.InventoryController;
import be.helha.labos.crystalclash.Controller.RouletteController;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.RouletteDAOImpl;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.RouletteService;
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
@SpringBootTest(classes = CrystalClashApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RouletteControllerTest {


    @Autowired
    private MockMvc mockMvc;

    private RouletteController rouletteController;

    @Autowired
    private InventoryService inventoryService;
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
                INSERT INTO users (username, password, level, cristaux, is_connected)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux)
            """);
            stmt.setString(1, "RouletteTestUser10" +
                    "");
            stmt.setString(2, "password");
            stmt.setInt(3, 1);
            stmt.setInt(4, 100);
            stmt.setBoolean(5, false);
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
        inventoryService.createInventoryForUser(username);

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
    @WithMockUser(username = "RouletteTestUser10")
    public void testPlayRoulette_success() throws Exception {
        mockMvc.perform(post("/roulette/play"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.objet").exists());
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
        mockMvc.perform(post("/roulette/play"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.objet").exists());

        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        MongoCollection<Document> collection = db.getCollection("Inventory");

        Document userInventory = collection.find(eq("username", "RouletteTestUser10")).first();

        assertNotNull(userInventory, "L'inventaire de l'utilisateur doit exister dans MongoDB");

        //Va chercher l mot clé objet dans l'inventaire et retourne l'objet
        //List car on sait deja que objet contient une liste de document
        @SuppressWarnings("unchecked")
        List<Document> items = (List<Document>) userInventory.get("objets");

        assertNotNull(items, "Le champ 'items' doit exister dans l'inventaire");
        assertFalse(items.isEmpty(), "L'inventaire ne doit pas être vide après avoir gagné un objet");
    }

    /*
    * Test pour voir si le joeur peut bien joué qu'une fois par jour
    * on appelle pour cela deux fois la requete
    * */
    @Test
    @Order(3)
    @DisplayName("Test si user peut ne bien joué qu une fois")
    @WithMockUser(username = "RouletteTestUser10")
    public void testPlayRoulette_onlyOncePerDay() throws Exception {
        // Premier appel : devrait réussir
        mockMvc.perform(post("/roulette/play"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.objet").exists());

        // Deuxième appel : devrait échouer
        mockMvc.perform(post("/roulette/play"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Déjà joué aujourd'hui gourmand va !"));
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