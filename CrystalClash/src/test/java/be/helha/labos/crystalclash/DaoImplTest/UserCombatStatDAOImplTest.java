package be.helha.labos.crystalclash.DaoImplTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.UserCombatStatDAOImpl;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Classe de test pour UserCombatStatDAOImpl.
 * Elle teste les opérations MongoDB liées aux statistiques de combat utilisateur :
 * - Création d'un document de stats
 * - Mise à jour après combat (avec/sans bazooka)
 * - Lecture des statistiques en JSON
 *
 * Les tests sont exécutés sur la base de données MongoDB de test.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserCombatStatDAOImplTest {

    private UserCombatStatDAOImpl dao;
    private MongoDatabase db;
    private static final String TEST_USERNAME = "CombatStatsTestUser";

    /**
     * Configure la connexion à la base MongoDB de test en redirigeant
     * la configuration de production vers celle de test.
     * Supprime les éventuels anciens documents du test utilisateur.
     */
    @BeforeEach
    public void setUp() throws Exception {
        var config = ConfigManager.getInstance().getConfig();
        var dbConfig = config.getAsJsonObject("db");

        var prodMongo = dbConfig.getAsJsonObject("MongoDBProduction");
        var testMongo = dbConfig.getAsJsonObject("MongoDBTest");

        prodMongo.getAsJsonObject("BDCredentials").entrySet().forEach(entry -> {
            String key = entry.getKey();
            prodMongo.getAsJsonObject("BDCredentials")
                .add(key, testMongo.getAsJsonObject("BDCredentials").get(key));
        });

        dao = new UserCombatStatDAOImpl();
        db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        db.getCollection("userCristauxWin").deleteOne(eq("username", TEST_USERNAME));
    }

    /**
     * Vérifie que la méthode createStatsForUser crée bien un document
     * initialisé avec des valeurs par défaut (0) dans MongoDB.
     */
    @Test
    @Order(1)
    @DisplayName("Création stats Mongo")
    public void testCreateStatsForUser() {
        dao.createStatsForUser(TEST_USERNAME);

        Document doc = db.getCollection("userCristauxWin").find(eq("username", TEST_USERNAME)).first();
        assertNotNull(doc);
        assertEquals(0, doc.getInteger("cristauxWin"));
        assertEquals(0, doc.getInteger("derniercombattour"));
        assertEquals(0, doc.getInteger("utilisationBazooka"));
    }

    /**
     * Vérifie que updateStatsAfterCombat met à jour les cristaux gagnés et le nombre
     * de tours, sans modifier le champ "utilisationBazooka".
     */
    @Test
    @Order(2)
    @DisplayName("Met à jour stats après combat (sans bazooka)")
    public void testUpdateStatsWithoutBazooka() {
        dao.createStatsForUser(TEST_USERNAME);
        dao.updateStatsAfterCombat(TEST_USERNAME, 100, 7);

        Document doc = db.getCollection("userCristauxWin").find(eq("username", TEST_USERNAME)).first();
        assertNotNull(doc);
        assertEquals(100, doc.getInteger("cristauxWin"));
        assertEquals(7, doc.getInteger("derniercombattour"));
        assertEquals(0, doc.getInteger("utilisationBazooka"));
    }

    /**
     * Vérifie que le champ "utilisationBazooka" est bien mis à 1 si la méthode
     * setBazookaUsed est appelée avant updateStatsAfterCombat.
     */
    @Test
    @Order(3)
    @DisplayName("Met à jour stats après combat avec bazooka utilisé")
    public void testUpdateStatsWithBazooka() {
        dao.createStatsForUser(TEST_USERNAME);
        dao.setBazookaUsed(TEST_USERNAME);
        dao.updateStatsAfterCombat(TEST_USERNAME, 200, 5);

        Document doc = db.getCollection("userCristauxWin").find(eq("username", TEST_USERNAME)).first();
        assertNotNull(doc);
        assertEquals(200, doc.getInteger("cristauxWin"));
        assertEquals(5, doc.getInteger("derniercombattour"));
        assertEquals(1, doc.getInteger("utilisationBazooka")); // doit être à 1 si bazooka utilisé
    }

    /**
     * Vérifie que la méthode getStats retourne un JSON valide et contenant les champs attendus.
     * Le JSON est retransformé en Document pour valider les champs de manière fiable.
     */
    @Test
    @Order(4)
    @DisplayName("Récupère les stats utilisateur")
    public void testGetStats() {
        dao.createStatsForUser(TEST_USERNAME);
        String json = dao.getStats(TEST_USERNAME);

        assertNotNull(json);

        org.bson.Document doc = org.bson.Document.parse(json);
        assertEquals(TEST_USERNAME, doc.getString("username"));
        assertEquals(0, doc.getInteger("cristauxWin"));
        assertEquals(0, doc.getInteger("derniercombattour"));
        assertEquals(0, doc.getInteger("utilisationBazooka"));
    }

}
