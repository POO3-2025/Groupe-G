package be.helha.labos.crystalclash.DaoImplTest;


import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.RouletteDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserDAOImpl;
import be.helha.labos.crystalclash.User.UserInfo;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
public class UserDAOImplTest {

    private UserDAOImpl dao;

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

        //Initialiser
        dao = new UserDAOImpl();
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
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "UserExist");
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

    @Order(1)
    @Test
    @DisplayName("Test Get username par son nom")
    public void getUserByUsername() throws Exception {
        Optional<UserInfo> user = dao.getUserByUsername("UserExist");
        assertTrue(user.isPresent());
        UserInfo userInfo = user.get();
        assertEquals("UserExist", userInfo.getUsername());
        assertEquals(5, userInfo.getLevel());
        assertEquals(100, userInfo.getCristaux());
        assertFalse(userInfo.isConnected());
    }

    @Order(2)
    @Test
    @DisplayName("Test Get d'un username qui n'existe pas")
    public void getUserByUsername_NotExist() throws Exception {
        Optional<UserInfo> user = dao.getUserByUsername("User_NotExist");
        assertTrue(user.isEmpty(), "L'utilisateur ne devrait pas être connue");
    }

    @Order(3)
    @Test
    @DisplayName("Test Update cristaux d'un user")
    public void updateCristaux() throws Exception {
        dao.updateCristaux("UserExist", 500);
        Optional<UserInfo> user = dao.getUserByUsername("UserExist");
        assertTrue(user.isPresent());
        assertEquals(500, user.get().getCristaux());
    }

    @Order(4)
    @Test
    @DisplayName("Test voir si un user est connecté")
    public void isAlreadyConnected() throws Exception {
        boolean connected = dao.isAlreadyConnected("UserExist");
        assertFalse(connected);
    }

    @Order(5)
    @Test
    @DisplayName("Test mettre a jour le boolean is_connected")
    public void updateIsConnected() throws Exception {
        dao.updateIsConnected("UserExist", true);
        assertTrue(dao.isAlreadyConnected("UserExist"));

    }

    @Order(6)
    @Test
    @DisplayName("Test échec update cristaux pour utilisateur inexistant")
    public void updateCristaux_userNotExist_shouldThrow() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            dao.updateCristaux("Inconnu", 999);
        });

        assertTrue(exception.getMessage().contains("Aucun utilisateur mis à jour"), "Le message doit indiquer qu'aucun utilisateur n'a été mis à jour");
    }

    @AfterAll
    public static void resetMySQLUsers() throws Exception {
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("DELETE FROM users");
        stmt.executeUpdate();
        stmt.close();
        conn.close();
        System.out.println("Tous les utilisateurs MySQL ont été supprimés.");

    }
}

