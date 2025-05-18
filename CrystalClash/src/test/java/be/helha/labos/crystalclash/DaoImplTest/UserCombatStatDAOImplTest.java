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
 * Classe de test pour {@link UserCombatStatDAOImpl}.
 * <p>
 * Elle vérifie le bon fonctionnement de l’implémentation DAO responsable
 * de la gestion des statistiques de combat utilisateur dans MongoDB.
 *
 * <ul>
 *     <li>Création initiale des stats utilisateur</li>
 *     <li>Mise à jour après combat (avec ou sans bazooka)</li>
 *     <li>Mise à jour de trophée (bronze/silver/or)</li>
 *     <li>Lecture des stats via JSON</li>
 * </ul>
 *
 * Tous les tests sont exécutés contre une base MongoDB de test.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserCombatStatDAOImplTest {

    private UserCombatStatDAOImpl dao;
    private MongoDatabase db;
    private static final String TEST_USERNAME = "CombatStatsTestUser";

    /**
     * Prépare la configuration de test avant chaque test.
     * Remplace dynamiquement les credentials de production par ceux de test pour MongoDB.
     * Supprime également tout document existant pour l'utilisateur de test.
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
     * Vérifie que {@code createStatsForUser} crée bien un document avec les champs par défaut :
     * cristauxWin = 0, derniercombattour = 0, utilisationBazooka = 0.
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
     * Vérifie que {@code updateStatsAfterCombat} met à jour les cristaux et le nombre de tours
     * sans modifier l'utilisation du bazooka (resté à 0 par défaut).
     */
    @Test
    @Order(2)
    @DisplayName("Met à jour stats après combat (sans bazooka)")
    public void testUpdateStatsWithoutBazooka() {
        dao.createStatsForUser(TEST_USERNAME);
        dao.updateStatsAfterCombat(TEST_USERNAME, 150, 8, "gagnant123");

        Document doc = db.getCollection("userCristauxWin").find(eq("username", TEST_USERNAME)).first();
        assertNotNull(doc);
        assertEquals(150, doc.getInteger("cristauxWin"));
        assertEquals(8, doc.getInteger("derniercombattour"));
        assertEquals(0, doc.getInteger("utilisationBazooka"));
    }

    /**
     * Vérifie que l'appel à {@code setBazookaUsed} active correctement le flag,
     * et que celui-ci est pris en compte lors du prochain {@code updateStatsAfterCombat}.
     */
    @Test
    @Order(3)
    @DisplayName("Met à jour stats après combat avec bazooka utilisé")
    public void testUpdateStatsWithBazooka() {
        dao.createStatsForUser(TEST_USERNAME);
        dao.setBazookaUsed(TEST_USERNAME);
        dao.updateStatsAfterCombat(TEST_USERNAME, 150, 8, "gagnant123");

        Document doc = db.getCollection("userCristauxWin").find(eq("username", TEST_USERNAME)).first();
        assertNotNull(doc);
        assertEquals(150, doc.getInteger("cristauxWin"));
        assertEquals(8, doc.getInteger("derniercombattour"));
        assertEquals(1, doc.getInteger("utilisationBazooka")); // doit être à 1 si bazooka utilisé
    }

    /**
     * Vérifie que {@code getStats} retourne un JSON contenant bien les champs
     * attendus pour un utilisateur nouvellement initialisé.
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

    /**
     * Vérifie que la méthode {@code updateStatsTrophy} active correctement le champ trophée spécifié (ex: "bronze").
     */
    @Test
    @Order(5)
    @DisplayName("Mise à jour du trophée Bronze")
    public void testUpdateStatsTrophy() {
        dao.createStatsForUser(TEST_USERNAME);

        // Appliquer la mise à jour pour le trophée "bronze"
        dao.updateStatsTrophy(TEST_USERNAME, "bronze");

        Document doc = db.getCollection("userCristauxWin").find(eq("username", TEST_USERNAME)).first();
        assertNotNull(doc, "Le document utilisateur doit exister");
        assertTrue(doc.getBoolean("bronze"), "Le trophée 'bronze' doit être à true");
    }

    /**
     * Vérifie l'effet cumulé de {@code setBazookaUsed} suivi de {@code updateStatsAfterCombat}.
     * Confirme que le champ {@code utilisationBazooka} passe bien à 1.
     */
    @Test
    @Order(6)
    @DisplayName("setBazookaUsed applique bien le flag à updateStatsAfterCombat")
    public void testSetBazookaUsedEffectively() {
        dao.createStatsForUser(TEST_USERNAME);

        // Déclenche l'usage du bazooka
        dao.setBazookaUsed(TEST_USERNAME);

        // Mise à jour des stats
        dao.updateStatsAfterCombat(TEST_USERNAME, 50, 6, "vainqueurTest");

        Document doc = db.getCollection("userCristauxWin").find(eq("username", TEST_USERNAME)).first();
        assertNotNull(doc, "Le document utilisateur doit exister");
        assertEquals(1, doc.getInteger("utilisationBazooka"), "utilisationBazooka doit être à 1 après usage");
        assertEquals(50, doc.getInteger("cristauxWin"), "Les cristaux doivent être mis à jour correctement");
        assertEquals(6, doc.getInteger("derniercombattour"), "Le nombre de tours doit être mis à jour correctement");
        assertEquals("vainqueurTest", doc.getString("dernierVainqueur"), "Le vainqueur doit être mis à jour correctement");
    }

    @AfterAll
    public static void resetMySQLUsers_AND_Mongo() throws Exception {
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("DELETE FROM users");
        stmt.executeUpdate();
        stmt.close();
        conn.close();
        System.out.println("Tous les utilisateurs MySQL ont été supprimés.");

        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");


        db.getCollection("Characters").deleteMany(new Document());
        // Ajoute d'autres collections si besoin
        db.getCollection("Inventory").deleteMany(new Document());

        System.out.println("Toutes les données Mongo ont été supprimées.");
    }
}
