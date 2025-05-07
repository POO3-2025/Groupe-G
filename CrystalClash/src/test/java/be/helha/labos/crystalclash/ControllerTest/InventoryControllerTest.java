package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.Controller.InventoryController;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.RegistreDAOimpl;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;

//Simulation d'un user connécter pour les tests qui en ont besoin
import be.helha.labos.crystalclash.Service.RegistreService;
import be.helha.labos.crystalclash.Service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryControllerTest {  // <- ici "Test" !

    private InventoryController inventoryController;
    private InventoryService inventoryService;
    private InventoryDAOImpl inventoryDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        // Forcer ConfigManager à utiliser les bases de données de test
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");

        var mongoProductionConfig = db.getAsJsonObject("MongoDBProduction");
        var mongoTestConfig = db.getAsJsonObject("MongoDBTest");

        mongoProductionConfig.getAsJsonObject("BDCredentials")
                .entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    mongoProductionConfig.getAsJsonObject("BDCredentials")
                            .add(key, mongoTestConfig.getAsJsonObject("BDCredentials").get(key));
                });

        var mysqlProductionConfig = db.getAsJsonObject("mysqlproduction");
        var mysqlTestConfig = db.getAsJsonObject("mysqltest");

        mysqlProductionConfig.getAsJsonObject("BDCredentials")
                .entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    mysqlProductionConfig.getAsJsonObject("BDCredentials")
                            .add(key, mysqlTestConfig.getAsJsonObject("BDCredentials").get(key));
                });

        // DAO
        var userDAO = new be.helha.labos.crystalclash.DAOImpl.UserDAOImpl();
        userService = new UserService(userDAO);

        inventoryDAO = new InventoryDAOImpl();
        inventoryDAO.setUserService(userService); //injection manuelle

        // Service
        inventoryService = new InventoryService(inventoryDAO);
        inventoryService.setUserService(userService); //injection manuelle

        // Controller
        inventoryController = new InventoryController();
        inventoryController.setInventoryService(inventoryService);
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

    @BeforeEach
    public void ensureUserExists() {
        try {
            var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
            var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux)
        """);
            stmt.setString(1, "inventoryTestUser");
            stmt.setString(2, "password");   // mot de passe bidon pour test
            stmt.setInt(3, 1);               // niveau de départ
            stmt.setInt(4, 100);            // cristaux de départ
            stmt.setBoolean(5, false); // connecté ou non
            stmt.setInt(6, 0); // valeur par défaut pour 'gagner'
            stmt.setInt(7, 0); // valeur par défaut pour 'perdu'
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Impossible d'insérer l'utilisateur de test en base MySQL");
        }
    }


    @Test
    @Order(1)
    @DisplayName("Test avoir inventaire d'un user")
    public void testGetInventory_existingUser() {
        // Arrange
        String username = "inventoryTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        inventoryDAO.createInventoryForUser(username);

        // Appelle de la méthode dans le controller
        Inventory inventory = inventoryController.getInventory(username);

        // Assert
        assertNotNull(inventory, "Inventory ne doit pas être null");
        assertEquals(0, inventory.getObjets().size(), "L'inventaire nouvellement créé doit être vide");
    }

    @Test
    @Order(2)
    @DisplayName("Test vendre un objet qui est dans l'inventaire")
    public void testSellObject_success() {
        String username = "inventoryTestUser";

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Nettoyer Mongo
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Ajouter un objet dans l'inventaire
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée de test", 100,5,5,5);
        objet.setType("Weapon");
        inventory.ajouterObjet(objet);

        // Sauvegarder l'inventaire
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Construire le payload comme dans un vrai appel POST dans le construct
        Map<String, String> payload = Map.of(
            "name", "Épée de test",
            "type", "Weapon"
        );

        // Appeler le controller
        var response = inventoryController.sellObject(payload);
        System.out.println("Réponse du contrôleur : " + response.getBody().getMessage());
        // vérif la réponse
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Objet vendu avec succès"));

        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) response.getBody().getData();

        assertEquals(50.0, ((Number) dataMap.get("gain")).doubleValue());
        assertEquals("medium", dataMap.get("rarity")); // car prix = 100
    }

    @Test
    @Order(3)
    @DisplayName("Test vendre un objet qui n'est pas dans l'inventaire")
    public void testSellObject_objectNotFound() {
        String username = "inventoryTestUser";

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        // Créer un inventaire vide
        inventoryDAO.createInventoryForUser(username);

        // Appeler le contrôleur avec un objet qui n'existe pas
        Map<String, String> payload = Map.of(
            "name", "ObjetInexistant",
            "type", "Weapon"
        );

        var response = inventoryController.sellObject(payload);
        System.out.println("Réponse (objet manquant) : " + response.getBody().getMessage());

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Objet non trouvé dans l'inventaire.", response.getBody().getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("Test vendre un objet avec utilisateur inexistant")
    public void testSellObject_userNotFound() {
        String username = "userInexistant";

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Supprimer l’utilisateur s’il existe
        try {
            var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
            var stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Impossible de supprimer l'utilisateur de test");
        }

        // Créer inventaire avec l’objet
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));
        inventoryDAO.createInventoryForUser(username);

        var inventory = inventoryDAO.getInventoryForUser(username);
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée de test", 100, 5, 5, 5);
        objet.setType("Weapon");
        inventory.ajouterObjet(objet);
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Appeler avec un vrai objet mais faux user
        Map<String, String> payload = Map.of(
            "name", "Épée de test",
            "type", "Weapon"
        );

        var response = inventoryController.sellObject(payload);
        System.out.println("Réponse (user inexistant) : " + response.getBody().getMessage());

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Utilisateur introuvable.", response.getBody().getMessage());
    }


    /**Test Estelle*/
    @Test
    @Order(5)
    @DisplayName("Test recupere un coffre a un user existant")
    public void testGetCoffre_existingUser() {
        // Arrange
        String username = "inventoryTestUser";

        // Simuler un utilisateur connecté
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);
        // Créer un coffre
        var coffre = new be.helha.labos.crystalclash.Object.CoffreDesJoyaux();
        coffre.setType("CoffreDesJoyaux");
        coffre.setName("Coffre test");
        coffre.setPrice(100);
        coffre.setReliability(10);
        coffre.setRequiredLevel(1);

        inventory.ajouterObjet(coffre);
        // Sauvegarder l'inventaire
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Act
        var response = inventoryController.getCoffre(username);

        // Assert
        assertNotNull(response, "La réponse ne doit pas être nulle");
        assertEquals(200, response.getStatusCodeValue(), "Le code HTTP attendu est 200 OK");
        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être nul");
        assertEquals("CoffreDesJoyaux", response.getBody().getType(), "Le type du coffre doit correspondre");

    }

    @Test
    @Order(6)
    @DisplayName("Test recupere un coffre a un user inexistant")
    public void testGetCoffre_nonExistingUser() {
        // Arrange
        String username = "userInexistant";

        // Simuler un utilisateur connecté
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);


        var response = inventoryController.getCoffre(username);

        // Assert
        assertEquals(404, response.getStatusCodeValue(), "Le code HTTP attendu est 404 Not Found");
    }

    @Test
    @Order(7)
    @DisplayName("Test: ajouter un objet dans le coffre")
    public void testAddObjectToCoffre() {
        // Arrange
        String username = "inventoryTestUser";

        // Simuler un utilisateur connecté
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Nettoyage Mongo
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", username));

        // Créer l’inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Créer le coffre
        var coffre = new be.helha.labos.crystalclash.Object.CoffreDesJoyaux();
        coffre.setType("CoffreDesJoyaux");
        coffre.setName("Coffre test");
        coffre.setPrice(100);
        coffre.setReliability(10);
        coffre.setRequiredLevel(1);

        // Créer un objet
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée de test", 100, 5, 5, 5);
        objet.setType("Weapon");

        // Ajouter les deux au backpack
        inventory.ajouterObjet(objet);
        inventory.ajouterObjet(coffre);
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Créer le payload pour simuler la requête POST
        Map<String, String> payload = Map.of(
                "name", "Épée de test",
                "type", "Weapon"
        );

        // Act : appel du contrôleur
        var response = inventoryController.addObjectToCoffre(username, payload);

        // Assert : vérifie la réponse HTTP et le message
        assertEquals(200, response.getStatusCodeValue(), "Le code HTTP attendu est 200 OK");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("succès"));

        // Vérifie que l’objet est maintenant dans le coffre
        Inventory updated = inventoryDAO.getInventoryForUser(username);
        // Vérifie que l’objet a été retiré de l’inventaire en regardant ce qui ya dans le coffre
        boolean objetDansCoffre = updated.getObjets().stream()
                // Filtre pour ne garder que les coffres
                .filter(o -> o instanceof CoffreDesJoyaux)
                // Récupère le coffre
                .map(o -> (CoffreDesJoyaux) o)
                // Récupère le contenu du coffre
                .flatMap(c -> c.getContenu().stream())
                // Vérifie si l’objet est dans le coffre
                .anyMatch(o -> o.getName().equals("Épée de test"));

        assertTrue(objetDansCoffre, "L’objet doit être dans le coffre après ajout");
    }

    @Test
    @Order(8)
    @DisplayName("test si paramètres manquants")
    public void testAddObjectToCoffre_TypeManquant() {
        String username = "inventoryTestUser";

        Map<String, String> payload = Map.of("name", "Épée de test"); // 'type' manquant

        var response = inventoryController.addObjectToCoffre(username, payload);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("requis"));
    }

    @Test
    @Order(9)
    @DisplayName("test si aucun coffre dans l'inventaire")
    public void testAddObjectToCoffre_NoCoffre() {
        String username = "inventoryTestUser";

        // Créer l’inventaire sans coffre
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée", 100, 5, 5, 5);
        objet.setType("Weapon");
        inventory.ajouterObjet(objet);
        inventoryDAO.saveInventoryForUser(username, inventory);

        Map<String, String> payload = Map.of("name", "Épée", "type", "Weapon");

        var response = inventoryController.addObjectToCoffre(username, payload);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("aucun coffre"));
    }

    @Test
    @Order(10)
    @DisplayName("Test ajout objet non présent dans inventaire")
    public void testAddObjectToCoffre_ObjectNotFound() {
        String username = "inventoryTestUser";
        inventoryDAO.createInventoryForUser(username);

        // Ajouter uniquement le coffre
        var coffre = new CoffreDesJoyaux();
        coffre.setType("CoffreDesJoyaux");
        coffre.setName("Coffre test");
        coffre.setPrice(100);
        coffre.setReliability(10);
        coffre.setRequiredLevel(1);

        Inventory inventory = inventoryDAO.getInventoryForUser(username);
        inventory.ajouterObjet(coffre);
        inventoryDAO.saveInventoryForUser(username, inventory);

        var payload = Map.of("name", "ObjetInexistant", "type", "Weapon");
        var response = inventoryController.addObjectToCoffre(username, payload);
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("objet non trouvé"));
    }


    @Test
    @Order(11)
    @DisplayName("test si coffre brisé")
    public void testAddObjectToCoffre_CoffreBroken() {
        String username = "inventoryTestUser";

        inventoryDAO.createInventoryForUser(username);

        var coffre = new CoffreDesJoyaux();
        coffre.setType("CoffreDesJoyaux");
        coffre.setName("Coffre cassé");
        coffre.setReliability(0); // brisé

        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée", 100, 5, 5, 5);
        objet.setType("Weapon");

        var inventory = inventoryDAO.getInventoryForUser(username);
        inventory.ajouterObjet(objet);
        inventory.ajouterObjet(coffre);
        inventoryDAO.saveInventoryForUser(username, inventory);

        Map<String, String> payload = Map.of("name", "Épée", "type", "Weapon");

        var response = inventoryController.addObjectToCoffre(username, payload);

        assertEquals(409, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("brisé"));
    }

    @Test
    @Order(12)
    @DisplayName("test si coffre plein")
    public void testAddObjectToCoffre_CoffrePlein() {
        String username = "inventoryTestUser";

        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        var coffre = new CoffreDesJoyaux();
        coffre.setType("CoffreDesJoyaux");
        coffre.setName("Coffre plein");
        coffre.setReliability(10);

        inventory.ajouterObjet(coffre);

        for (int i = 0; i < coffre.getMaxCapacity(); i++) {
            var item = new be.helha.labos.crystalclash.Object.Weapon("Item" + i, 10, 1, 1, 1);
            item.setType("Weapon");
            coffre.AddObjects(item);
        }

        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée", 100, 5, 5, 5);
        objet.setType("Weapon");

        inventory.ajouterObjet(objet);
        inventoryDAO.saveInventoryForUser(username, inventory);

        Map<String, String> payload = Map.of("name", "Épée", "type", "Weapon");

        var response = inventoryController.addObjectToCoffre(username, payload);

        assertEquals(409, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("plein"));
    }


    @AfterEach
    public void cleanUp() {
        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Inventory").deleteMany(new org.bson.Document("username", "inventoryTestUser"));
    }




}
