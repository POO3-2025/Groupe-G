package be.helha.labos.crystalclash.DaoImplTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.RegistreDAOimpl;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegistreDAOimplTest {

    private RegistreDAOimpl registreDAOimpl;

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

        var mysqlProductionConfig = db.getAsJsonObject("mysqlproduction");
        var mysqlTestConfig = db.getAsJsonObject("mysqltest");

        mysqlProductionConfig.getAsJsonObject("BDCredentials")
                .entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    mysqlProductionConfig.getAsJsonObject("BDCredentials")
                            .add(key, mysqlTestConfig.getAsJsonObject("BDCredentials").get(key));
                });

        //Initialiser redistreDao avant
        registreDAOimpl = new RegistreDAOimpl();
    }

    /*
    * Insertion d un user test pour le test userExist
    * */
    @BeforeEach
    public void ensureUserExists() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive	)
            VALUES (?, ?, ?, ?, ?, ?, ?,?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "TestUserExist");
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


    }

    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    /*
    * Test insertion d un user
    * */
    @Test
    @Order(1)
    @DisplayName("Test ajout de user")
    public void InsertUser(TestInfo testInfo) throws Exception {

        String username = "CelioTest";
        String hashedPassword  = "CelioTest";

        registreDAOimpl.insertUser(username,hashedPassword);

        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqltest")) {
            PreparedStatement stmt = conn.prepareStatement("Select * from users where username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next(), "L'utilisateur doit être inséré.");
            assertEquals(username, rs.getString("username"));
            assertEquals(hashedPassword, rs.getString("password"));
            assertEquals(1, rs.getInt("level"));
            assertEquals(100, rs.getInt("cristaux"));
            assertFalse(rs.getBoolean("is_connected"));

        }
    }
    /*
     * Test insertion d un user
     * */
    @Test
    @Order(1)
    @DisplayName("Test ajout de user")
    public void InsertUserTest(TestInfo testInfo) throws Exception {

        String username = "CelioTest";
        String hashedPassword  = "CelioTest";

        registreDAOimpl.userExists(username);

    }


    @AfterAll
    public void resetMySQLUsers() throws Exception {
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("DELETE FROM users");
        stmt.executeUpdate();
        stmt.close();
        conn.close();
        System.out.println("Tous les utilisateurs MySQL ont été supprimés.");

    }


}
