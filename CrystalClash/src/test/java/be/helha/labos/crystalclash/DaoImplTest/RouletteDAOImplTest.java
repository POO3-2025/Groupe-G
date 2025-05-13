package be.helha.labos.crystalclash.DaoImplTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.RouletteDAOImpl;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
public class RouletteDAOImplTest {

    private RouletteDAOImpl dao;



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
        dao = new RouletteDAOImpl();
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
        stmt.setString(1, "testuseRoulette");
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
    @DisplayName("Test mise à jour et récupération de la date de dernière roulette")
    public void testUpdateAndGetLastPlayDate() throws Exception {

        //Init la date d aoujourd'hui
        LocalDate today = LocalDate.now();

        // Met à jour la date
        dao.UpdateLastPlayDate("testuseRoulette", today);

        // Récupère la date
        LocalDate retrieved = dao.getLastPlayDate("testuseRoulette");


        assertNotNull(retrieved);
        assertEquals(today, retrieved);
    }


    /*
     * test pour obtenir la derniere date jouée par un user
     * */
    @Order(2)
    @Test
    @DisplayName("Test obtenir la dernier date joué par le joueur")
    public void testgetLastPlayDatee() throws Exception {

        LocalDate today = LocalDate.now();//Depend du fuseau horaire
        LocalDate result = dao.getLastPlayDate("testuseRoulette");
        System.out.println("Date récupérée depuis Mongo : " + result);
        assertNotNull(result, "La date ne doit pas être nulle");
        assertTrue(result.equals(today), "La date récupérée doit être égale à celle insérée");

    }

    /*
     * test pour obtenir la derniere date qui null d'un joueur qui n'a jamais joué
     * */
    @Order(3)
    @Test
    @DisplayName("Test getLastPlayDate sans avoir joué")
    public void testGetLastPlayDate_noData() {
        LocalDate result = dao.getLastPlayDate("UserQuiNaJamaisJoue");
        System.out.println("Date récupérée depuis Mongo : " + result);
        assertNull(result, "La date doit être null si aucun jeu n'a été joué");
    }
}
