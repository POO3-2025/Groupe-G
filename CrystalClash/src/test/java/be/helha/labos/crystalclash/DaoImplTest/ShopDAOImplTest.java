package be.helha.labos.crystalclash.DaoImplTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.ShopDAOImpl;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.Service.InventoryService;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

public class ShopDAOImplTest {

    private ShopDAOImpl dao;
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

        // DAO et services, instanciation et injection manuelle car pas de spring ici donc ça ne peut pas aller rechercher automatiquement
        var userDAO = new be.helha.labos.crystalclash.DAOImpl.UserDAOImpl(); //Acceder au user ds la db
        UserService userService = new UserService(userDAO); //Crée le service métier

        dao = new ShopDAOImpl(); //Crée dao
        dao.setUserService(userService); //injection manuelle du user dans le shop (savoir les infos du user pour l'achat)

        //Gere l'inventaire ds Mongo et injection de userService(actions dans l'inventaire ont besoin besoins des données du user update)
        inventoryDAO = new InventoryDAOImpl();
        inventoryDAO.setUserService(userService);


        InventoryService inventoryService = new InventoryService(inventoryDAO);
        dao.setInventoryService(inventoryService);//Inject de l'inventaire ds le shop (verif si il est plein et de placer l objet dedans)
    }

    @BeforeEach
    public void cleanInventory() {
        var db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        db.getCollection("Inventory").deleteOne(eq("username", "testuseShop"));
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
        stmt.setString(1, "testuseShop");
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
    @DisplayName("Test obtenir le shop et trié")
    public void testGetShopItems_returnsFormattedObjects() {

        List<Map<String, Object>> shopItems = dao.getShopItems();

        //Vérif sa la boutique n'est pas vide
        assertFalse(shopItems.isEmpty(), "La boutique ne doit pas être vide");

        //Vérif si les champs sont bien dans le shop
        for (Map<String, Object> shopItem : shopItems) {
            assertTrue(shopItem.containsKey("type"),"L'objet doit contenir un champ type");
            assertTrue(shopItem.containsKey("name"),"L'objet doit contenir un champ name");
            assertTrue(shopItem.containsKey("price"),"L'objet doit contenir un champ price");
            assertTrue(shopItem.containsKey("requiredLevel"),"L'objet doit contenir un champ requiredLevel");

            assertTrue(shopItem.get("price") instanceof Integer,"Le champ price doit etre un entier");
            assertTrue(shopItem.get("requiredLevel") instanceof Integer, "Le champ requiredLevel doit etre un entier");


            for (int i = 1; i < shopItems.size(); i++){
                //i -1 car al boucle commence a 1
                //shopItems est une map (get peut etre utiliser)
                int levelPrev   = (int) shopItems.get(i-1).get("requiredLevel"); //recup l'objet avant
                int levelCurr = (int) shopItems.get(i).get("requiredLevel");//Recup l obejt courant, le bon
                assertTrue(levelPrev <= levelCurr, "Les objets doivent être triés par 'requiredLevel'"); //La compare si levelPrev est bien plus petit ou egale a l'objet courant

            }
        }

    }

    @Order(2)
    @Test
    @DisplayName("Test achat d'un objet dans la boutique")
    public void testBuyItem_success() {

        inventoryDAO.createInventoryForUser("testuseShop");
        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        var doc = db.getCollection("Inventory").find(eq("username", "InventoryTestUser")).first();

        String result = dao.buyItem("testuseShop","Epee en bois", "Weapon");
        System.out.println("Résultat achat : " + result);
        assertTrue(result.toLowerCase().contains("acheté"),"Le message doit comfirmer l'achat");

        Inventory loaded = inventoryDAO.getInventoryForUser("testuseShop");
        assertNotNull(loaded);
        assertEquals(1, loaded.getObjets().size());
        // Vérifie que l'objet est bien une Weapon avec les bons attributs
        var obj = loaded.getObjets().get(0);
        assertTrue(obj instanceof be.helha.labos.crystalclash.Object.Weapon, "L'objet doit être une arme");
        assertEquals("Epee en bois", obj.getName());
        assertEquals(50, obj.getPrice());

    }
    }


