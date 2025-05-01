package be.helha.labos.crystalclash.ConfigManagerTest;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test du configManager
 * Test pour voir sir le config est bien lue et pas vide
 * Test pour se connecter a la db de test Mongo
 * Test pour se connecter a la db de test Mysql
 * **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigManagerTest {

    private ConfigManager configManager;

    @BeforeAll
    public void setup() {
        configManager = ConfigManager.getInstance();

        // Forcer la création de la base MongoDBTest
        MongoDatabase db = configManager.getMongoDatabase("MongoDBTest");
        db.getCollection("init_test_collection").insertOne(new Document("init", true));
        db.getCollection("init_test_collection").drop();

        System.out.println("Setup MongoDBTest terminé avec succès.");
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

    @Test
    @Order(2)
    @DisplayName("Test lecture du fichier de configuration")
    void testConfigLoading(){
        assertNotNull(configManager.getConfig(), "La config ne doit pas être null");
        System.out.println(" testConfigLoading : lecture de la config OK.");
    }

    @Test
    @Order(3)
    @DisplayName("Test connexion à MongoDBTest")
    void testMongoDBTestConnection() {
        MongoDatabase db = configManager.getMongoDatabase("MongoDBTest");
        assertNotNull(db, "La base MongoDB ne doit pas être nulle.");
        assertEquals("MongoDBTest", db.getName(), "Le nom de la base MongoDB doit être correct.");
        System.out.println("testMongoDBTestConnection : connexion Mongo OK.");
    }

    @Test
    @Order(4)
    @DisplayName("Test connexion à MysqlTest")
    void testMysqlTestConnexion() {
        try (Connection conn = configManager.getSQLConnection("mysqltest")) {
            assertNotNull(conn, "La connexion MySQL ne doit pas être nulle.");
            assertTrue(conn.isValid(2), "La connexion MySQL doit être valide.");
            System.out.println("testMysqlTestConnexion : connexion MySQL OK.");
        } catch (SQLException e) {
            fail(" Erreur lors de la connexion du test : " + e.getMessage());
        }
    }
    @Test
    @Order(5)
    @DisplayName("Test d'erreur avec une clé de config invalide")
    void testConnexionAvecCleInvalide() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            configManager.getMongoDatabase("cleInvalide");
        });
        String message = exception.getMessage();
        assertTrue(message.contains("Erreur de connexion à MongoDB") || message.contains("null"),
            "Le message doit indiquer une erreur de connexion");
        System.out.println("testConnexionAvecCleInvalide : exception correctement levée.");
    }
    @Test
    @Order(6)
    @DisplayName("Test d'erreur SQL avec une clé de config invalide")
    void testSQLConnexionAvecCleInvalide() {
        Exception exception = assertThrows(SQLException.class, () -> {
            configManager.getSQLConnection("cleMysqlInvalide");
        });
        System.out.println("testSQLConnexionAvecCleInvalide : exception correctement levée.");
    }

}
