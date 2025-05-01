package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.server_auth.CrystalClashApplication;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = CrystalClashApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    public void ensureUserExists_and_mongo() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "ShopTestUser");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }


    @BeforeEach
    public void EnsureInventoryMongo() throws Exception {
        // Crée un inventaire
        // Créer inventaire avec l’objet
        String username = "ShopTestUser";
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));
        inventoryService.createInventoryForUser(username);

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
    @WithMockUser(username = "ShopTestUser")
    public void testGetShopObjets() throws Exception {
        mockMvc.perform(get("/shop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) // le $ représente la racine du doc json (contient l'objet ou tb json retourné par la reponse de la requete)
                .andExpect(jsonPath("$[0].name").exists()); //$[0] premier élém du tb, champ name la
    }


    /*
     * Test pour acheté un objet du shop
     * */
    @Test
    @Order(2)
    @DisplayName("Test achat objet")
    @WithMockUser(username = "ShopTestUser")
    public void testBuyItem_success() throws Exception {
        mockMvc.perform(post("/shop/buy")
                //On doit passer le vrai json un peu comment postman quoi
                        .contentType("application/json")
                        .content("""
                        {
                "name": "Epee en bois",
                "type": "Weapon"
                }
                  """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("acheté")));
    }

    @Test
    @Order(3)
    @DisplayName("Test achat objet avec 0 cristaux")
    @WithMockUser(username = "ShopTestUser")
    public void testBuyItem_notEnoughCrystals() throws Exception {
        // Mettre le joueur à 0 cristaux
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("UPDATE users SET cristaux = 0 WHERE username = ?");
        stmt.setString(1, "ShopTestUser");
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        mockMvc.perform(post("/shop/buy")
                        //On doit passer le vrai json un peu comment postman quoi
                        .contentType("application/json")
                        .content("""
                        {
                "name": "Epee en bois",
                "type": "Weapon"
                }
                  """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Pas assez de cristaux !"));
    }

    @Test
    @Order(3)
    @DisplayName("Test achat deux fois Coffre")
    @WithMockUser(username = "ShopTestUser")
    public void testBuy_Two_CoffreDesJoyaux() throws Exception {
        mockMvc.perform(post("/shop/buy")
                        //On doit passer le vrai json un peu comment postman quoi
                        .contentType("application/json")
                        .content("""
                        {
                "name": "Coffre des Joyaux",
                "type": "CoffreDesJoyaux"
                }
                  """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("acheté")));

        //Achat une seconde fois
        mockMvc.perform(post("/shop/buy")
                        //On doit passer le vrai json un peu comment postman quoi
                        .contentType("application/json")
                        .content("""
                        {
                "name": "Coffre des Joyaux",
                "type": "CoffreDesJoyaux"
                }
                  """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tu possèdes déjà un Coffre des Joyaux !"));
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
