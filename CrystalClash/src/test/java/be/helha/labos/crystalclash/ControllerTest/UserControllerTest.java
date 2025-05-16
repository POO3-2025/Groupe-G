package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Controller.UserController;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserCombatStatDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserDAOImpl;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.UserCombatStatService;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.User.UserInfo;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test pour le contrôleur UserController.
 *teste les endpoints GET liés aux utilisateurs :
 *
 * Aucun mock n’est utilisé ici, tout passe par les vraies bases de test MySQL/Mongo.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    private UserController userController;

    /**
     * Configure manuellement les DAO et services, et force le projet à pointer
     * vers les bases de données de test (MongoDBTest et mysqltest).
     */
    @BeforeEach
    public void setup() throws Exception {
        // Redirection MySQL prod -> test
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");

        var mysqlProd = db.getAsJsonObject("mysqlproduction");
        var mysqlTest = db.getAsJsonObject("mysqltest");
        mysqlProd.getAsJsonObject("BDCredentials").entrySet().forEach(entry ->
                mysqlProd.getAsJsonObject("BDCredentials")
                        .add(entry.getKey(), mysqlTest.getAsJsonObject("BDCredentials").get(entry.getKey()))
        );

        // Redirection MongoDB prod = test
        var mongoProd = db.getAsJsonObject("MongoDBProduction");
        var mongoTest = db.getAsJsonObject("MongoDBTest");
        mongoProd.getAsJsonObject("BDCredentials").entrySet().forEach(entry ->
                mongoProd.getAsJsonObject("BDCredentials")
                        .add(entry.getKey(), mongoTest.getAsJsonObject("BDCredentials").get(entry.getKey()))
        );

        // Création manuelle des services et injection dans le contrôleur
        UserCombatStatService userCombatStatService = new UserCombatStatService(new UserCombatStatDAOImpl());
        UserDAOImpl userDAOImpl = new UserDAOImpl();
        userDAOImpl.setUserCombatStatService(userCombatStatService);
        UserService userService = new UserService(userDAOImpl);
        CharacterService characterService = new CharacterService(new CharacterDAOImpl());

        userController = new UserController();
        userController.setCharacterService(characterService);
        userController.setUserService(userService);
        userController.setUserCombatStatService(userCombatStatService);
    }

    /**
     * Nettoie les collections Mongo liées aux personnages et inventaires
     * pour éviter toute interférence entre les tests.
     */
    @BeforeEach
    public void cleanupMongo() {
        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        db.getCollection("Characters").deleteMany(new Document());
        db.getCollection("Inventory").deleteMany(new Document());
    }

    /**
     * Crée un utilisateur test en base MySQL + ajoute un personnage sélectionné en MongoDB
     *  initialise les stats de combat pour l’utilisateur dans Mongo.
     */
    @BeforeEach
    public void ensureUserExists() throws Exception {
        // Ajout en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected, gagner, perdu, Winconsecutive)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "userControllerTest");
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

        // Ajout du personnage Mongo sélectionné
        MongoDatabase mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").insertOne(new Document()
                .append("username", "userControllerTest")
                .append("type", "troll")
                .append("backpack", "backpack")
                .append("selected", true)
        );

        // Ajout stats Mongo pour éviter 404
        new UserCombatStatDAOImpl().createStatsForUser("userControllerTest");
    }

    /**
     * Vérifie que le endpoint GET /user/{username} renvoie les infos utilisateur
     * + le personnage sélectionné (type Mongo).
     */
    @Test
    @Order(1)
    @DisplayName("GET /user/{username} renvoie UserInfo avec perso sélectionné")
    public void testGetUserByUsername() {
        var response = userController.getUserByUsername("userControllerTest");

        assertEquals(200, response.getStatusCodeValue(), "Le code de retour doit être 200");
        assertTrue(response.getBody() instanceof UserInfo, "Le corps doit être un UserInfo");

        var info = (UserInfo) response.getBody();
        assertEquals("userControllerTest", info.getUsername());
        assertEquals("troll", info.getSelectedCharacter(), "Le perso sélectionné doit être 'troll'");
    }

    /**
     * Vérifie que le endpoint GET /user/stats/{username} retourne bien un JSON
     * avec les stats Mongo de l’utilisateur.
     */
    @Test
    @Order(2)
    @DisplayName("GET /user/stats/{username} retourne JSON des stats")
    public void testGetStats() {
        var response = userController.getStats("userControllerTest");

        assertEquals(200, response.getStatusCodeValue(), "Le code doit être 200");
        String json = response.getBody();

        assertNotNull(json, "Le JSON ne doit pas être null");
        var doc = Document.parse(json);
        assertEquals("userControllerTest", doc.getString("username"));
        assertEquals(0, doc.getInteger("cristauxWin"));
        assertEquals(0, doc.getInteger("utilisationBazooka"));
    }

    /**
     * Vérifie que GET /user/{username} renvoie 404 si l’utilisateur n’existe pas en base.
     */
    @Test
    @Order(3)
    @DisplayName("GET /user/{username} renvoie 404 si utilisateur inexistant")
    public void testUserNotFound() {
        var response = userController.getUserByUsername("inconnuXYZ");
        assertEquals(404, response.getStatusCodeValue(), "Doit renvoyer 404 si l'utilisateur est inconnu");
    }

    /**
     * Vérifie que GET /user/stats/{username} renvoie 404 si aucun document de stats trouvé dans Mongo.
     */
    @Test
    @Order(4)
    @DisplayName("GET /user/stats/{username} renvoie 404 si stats absentes")
    public void testStatsNotFound() {
        var response = userController.getStats("unknownUser");
        assertEquals(404, response.getStatusCodeValue(), "Doit renvoyer 404 si stats absentes");
    }
}