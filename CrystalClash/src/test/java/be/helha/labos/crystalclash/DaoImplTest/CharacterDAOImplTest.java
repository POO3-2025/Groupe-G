package be.helha.labos.crystalclash.DaoImplTest;


import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.RegistreDAOimpl;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterDAOImplTest {

    private CharacterDAOImpl dao;



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
        dao = new CharacterDAOImpl();
    }

    /*
     * Insertion d un user test pour le test userExist
     * */
    @BeforeEach
    public void ensureUserExists() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "testusegetcharacter");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        MongoDatabase mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        // Insère un personnage sélectionné
        Document character = new Document("username", "TestUser")
                .append("type", "troll")
                .append("selected", true);
        mongo.getCollection("Characters").insertOne(character);

    }

    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    @Test
    @Order(1)
    @DisplayName("Test récupération du personnage sélectionné")
    public void testGetCharacterForUser() {
        // Redirige vers la base de test temporairement
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        //Surchargement de la méthode getCharacterForUser pour qu elle utilise mongoTest la on recherche par rapport au username et au selected a true
        dao = new CharacterDAOImpl() {
            @Override
            public String getCharacterForUser(String username) {
                MongoCollection<Document> collection = mongoTest.getCollection("Characters"); //Obtenir la collection Characters pour allez rechercher dedans
                Document result = collection.find(
                        new Document("username", username).append("selected", true)
                ).first();
                return result != null ? result.getString("type") : null; //Vérif juste si un perso a bien été trouvé pour luti, si oui alors on retourne son type si pas bah null (Aucun perso trouvé)
            }
        };

        String character = dao.getCharacterForUser("TestUser"); //va cherche ds mongo le doc avec TestUser et true
        assertEquals("troll", character); //verif que la valeur retournée est bien le troll la
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

        System.out.println("Toutes les données Mongo ont été supprimées.");
    }
}
