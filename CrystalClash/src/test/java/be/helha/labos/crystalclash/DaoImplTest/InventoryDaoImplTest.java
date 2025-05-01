package be.helha.labos.crystalclash.DaoImplTest;

import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Object.Weapon;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InventoryDaoImplTest {


    private InventoryDAOImpl inventoryDAO;


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
        inventoryDAO = new InventoryDAOImpl();
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
        stmt.setString(1, "InventoryTestUser");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        MongoDatabase mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        // Insère un personnage sélectionné

    }

    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    @Test
    @Order(1)
    @DisplayName("Test création inventaire")
    public void TestCreateInventory() throws Exception {
        inventoryDAO.createInventoryForUser("InventoryTestUser");

        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        var doc = db.getCollection("Inventory").find(eq("username", "InventoryTestUser")).first();

        assertNotNull(doc, "L'inventaire doit être créé dans MongoDB.");
    }

    @Test
    @Order(2)
    @DisplayName("Test sauverr inventaire")
    public void TestSaveInventoryAND_Get() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setUsername("InventoryTestUser");
        Weapon weapon = new Weapon("Epee en bois",23,1,2,5);
        weapon.setName("Epee en bois");
        weapon.setType("Weapon");
        inventory.ajouterObjet(weapon);

        InventoryDAOImpl dao = new InventoryDAOImpl();
        dao.saveInventoryForUser("InventoryTestUser", inventory);

        Inventory loaded = dao.getInventoryForUser("InventoryTestUser");

        //On va inserer la
        assertNotNull(loaded);
        assertEquals(1, loaded.getObjets().size());

        ObjectBase obj = loaded.getObjets().get(0);
        assertTrue(obj instanceof Weapon);
        assertEquals("Epee en bois", obj.getName());
        assertEquals(23,obj.getPrice());
    }

//Test coffre en vif
    @Test
    @Order(3)
    @DisplayName("Test ajout coffre et le recup")
    public void testGetCoffreDesJoyauxForUser() {
        Inventory inventory = new Inventory();
        inventory.setUsername("InventoryTestUser");

        //Créa du coffre et mettre dans l'iventaire
        CoffreDesJoyaux coffre = new CoffreDesJoyaux();
        coffre.setName("Coffre test");
        coffre.setType("CoffreDesJoyaux");
        coffre.setReliability(5);
        coffre.setRequiredLevel(1);
        inventory.ajouterObjet(coffre);

        inventoryDAO.saveInventoryForUser("InventoryTestUser", inventory);
        CoffreDesJoyaux found = inventoryDAO.getCoffreDesJoyauxForUser("InventoryTestUser");

        assertNotNull(found);
        assertEquals("Coffre test", found.getName());

    }

    @Test
    @Order(3)
    @DisplayName("Test ajouté objet ds coffre")
    public void testAddObjectToCoffre_success() {
        Inventory inventory = new Inventory();
        inventory.setUsername("InventoryTestUser");

        //Créa du coffre et mettre dans l'iventaire
        CoffreDesJoyaux coffre = new CoffreDesJoyaux();
        coffre.setName("Coffre test");
        coffre.setType("CoffreDesJoyaux");
        coffre.setReliability(5);
        coffre.setRequiredLevel(1);
        inventory.ajouterObjet(coffre);

        Weapon weapon = new Weapon("Epee en bois",23,1,2,5);
        weapon.setName("Epee en bois");
        weapon.setType("Weapon");
        inventory.ajouterObjet(weapon);

        inventoryDAO.saveInventoryForUser("InventoryTestUser", inventory);

        var res = inventoryDAO.addObjectToCoffre("InventoryTestUser", "Epee en bois", "Weapon");
        assertTrue(res.getMessage().contains("succès"));

    }
}
