package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Controller.CharactersController;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.Equipment;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserService;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


import static org.junit.jupiter.api.Assertions.*;



public class CharacterTest {


    private CharactersController charactersController;
    private CharacterService characterService;
    private CharacterDAO characterDAO;
    private UserService userService;
    private InventoryService inventoryService;
    private InventoryDAOImpl inventoryDAO;

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

        // DAOs & Services
        var userDAO = new be.helha.labos.crystalclash.DAOImpl.UserDAOImpl();
        userService = new UserService(userDAO);

        inventoryDAO = new InventoryDAOImpl();
        inventoryDAO.setUserService(userService);

        inventoryService = new InventoryService(inventoryDAO);
        inventoryService.setUserService(userService);

        characterDAO = new CharacterDAOImpl();
        characterDAO.setInventoryService(inventoryService);

        characterService = new CharacterService(characterDAO);

        // Contrôleur
        charactersController = new CharactersController();
        charactersController.setCharacterServices(characterService);


    }

    @BeforeEach
    public void cleanupMongo() {
        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        db.getCollection("Characters").deleteMany(new Document());
        db.getCollection("Inventory").deleteMany(new Document());
    }


    @BeforeEach
    public void ensureUserExists_and_mongo() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
        INSERT INTO users (username, password, level, cristaux, is_connected, gagner, perdu,Winconsecutive	)
        VALUES (?, ?, ?, ?, ?, ?, ?,?)
        ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
    """);
        stmt.setString(1, "CharacterTestUser");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.setInt(6, 0);
        stmt.setInt(7, 0);
        stmt.setInt(8, 0);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    @Test
    @Order(1)
    @DisplayName("Test de la création d'un personnage")
    public void testSelectCharacter() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        assertNotNull(character, "Le personnage ne doit pas être null");
        assertEquals("Aquaman", character.getName(), "Le nom du personnage doit être Aquaman");
        assertEquals(85, character.getPV(), "Les PV du personnage doivent être 85");
    }

    @Test
    @Order(2)
    @DisplayName("Test de la recperation d'un personnage")
    public void testGetCharacter() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        // Insérer un personnage avec un backpack vide
        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());
        characterService.createEquipmentForCharacter(username, character.getClass().getSimpleName());
        characterService.setSelectedCharacter(username, character.getClass().getSimpleName());

        String selectedCharacter = characterService.getCharacterForUser(username);
        assertEquals("Aquaman", selectedCharacter, "Le personnage récupéré doit être Aquaman");

    }


    @Test
    @Order(3)
    @DisplayName("Test récupération d'un backpack")
    public void testGetBackpack() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());
        BackPack backpackGet = characterService.getBackPackForCharacter(username);

        assertNotNull(backpackGet, "Le backpack doit être null");
        assertTrue(backpackGet.getObjets().isEmpty(), "Le backpack doit être vide après la création");
    }

    @Test
    @Order(4)
    @DisplayName("Test d'ajout d'un objet du backpack")
    public void testaddFromBackpack() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Ajouter un objet dans l'inventaire
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Epee de test", 100,5,5,5);
        objet.setType("Weapon");
        inventory.ajouterObjet(objet);

        // Sauvegarder l'inventaire
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Ajout de l’objet au backpack
        characterService.addObjectToBackPack(username, objet.getName(), objet.getType());

        // Vérification du contenu du backpack
        BackPack backpack = characterService.getBackPackForCharacter(username);
        assertNotNull(backpack, "Le backpack ne doit pas être null");
        assertEquals(1, backpack.getObjets().size(), "Le backpack doit contenir un objet");
        assertEquals("Epee de test", backpack.getObjets().get(0).getName(), "L'objet doit être correctement ajouté");
    }

    @Test
    @Order(5)
    @DisplayName("Test remove objet from backpack")
    public void testRemoveObjectToCoffre() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        inventoryDAO.createInventoryForUser(username);

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());
        BackPack backpack = characterService.getBackPackForCharacter(username);

        // Ajouter un objet dans le backpack
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Epee de test", 100,5,5,5);
        objet.setType("Weapon");
        backpack.AddObjects(objet);

        characterService.saveBackPackForCharacter(username, backpack);

        characterService.removeObjectFromBackPack(username, objet.getName());
        // Vérification du contenu du backpack
        BackPack backpackAfter = characterService.getBackPackForCharacter(username);
        assertNotNull(backpackAfter, "Le backpack doit être null");
        assertEquals(0, backpackAfter.getObjets().size(), "Le backpack doit être vide après la suppression de l'objet");

        // Vérification que l'objet a été mis dans l'inventaire
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        assertNotNull(inventory, "L'inventaire ne  doit pas être null");
        assertEquals(1, inventory.getObjets().size(), "L'inventaire doit contenir un objet");
        assertEquals("Epee de test", inventory.getObjets().get(0).getName(), "L'objet doit être correctement ajouté à l'inventaire");

    }

    @Test
    @Order(6)
    @DisplayName("Test d'ajout d'un objet dans le coffre")
    public void testAddCoffre() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        //mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());
        BackPack backpack = characterService.getBackPackForCharacter(username);

        // Ajouter un objet dans le backpack
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Epee de test", 100,5,5,5);
        objet.setType("Weapon");
        backpack.AddObjects(objet);

        characterService.saveBackPackForCharacter(username, backpack);

        // Créer un coffre
        var coffre = new be.helha.labos.crystalclash.Object.CoffreDesJoyaux();
        coffre.setType("CoffreDesJoyaux");

        // Ajouter le coffre au backpack
        backpack.AddObjects(coffre);
        characterService.saveBackPackForCharacter(username, backpack);

        // Ajouter l'objet au coffre
        characterService.addObjectToCoffre(username, objet.getName(), objet.getType());
        // Vérification du contenu du coffre
        BackPack backpackAfter = characterService.getBackPackForCharacter(username);
        assertNotNull(backpackAfter, "Le backpack doit être null");
        assertEquals(1, backpackAfter.getObjets().size(), "Le backpack doit contenir un coffre");
        assertEquals(1, ((be.helha.labos.crystalclash.Object.CoffreDesJoyaux) backpackAfter.getObjets().get(0)).getContenu().size(), "Le coffre doit contenir un objet");
        assertEquals("Epee de test", ((be.helha.labos.crystalclash.Object.CoffreDesJoyaux) backpackAfter.getObjets().get(0)).getContenu().get(0).getName(), "L'objet doit être correctement ajouté au coffre");

    }

    @Test
    @Order(7)
    @DisplayName("Echec- Test d'ajout d'un objet dans le coffre - coffre plein")
    public void testCoffrePlein() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());
        BackPack backpack = characterService.getBackPackForCharacter(username);

        // Créer un coffre
        var coffre = new be.helha.labos.crystalclash.Object.CoffreDesJoyaux();
        coffre.setType("CoffreDesJoyaux");

        // Remplir le coffre jusqu'à sa capacité max
        for (int i = 1; i <= coffre.getMaxCapacity(); i++) {
            var obj = new be.helha.labos.crystalclash.Object.Weapon("Objet " + i, 10 * i, 1, 5, 5);
            obj.setType("Weapon");
            coffre.getContenu().add(obj);
        }

        // Ajouter le coffre plein au backpack
        backpack.AddObjects(coffre);
        // Ajouter un objet qu'on va tenter d'ajouter dans le coffre
        var objetEnTrop = new be.helha.labos.crystalclash.Object.Weapon("Objet de trop", 999, 1, 5, 5);
        objetEnTrop.setType("Weapon");
        backpack.AddObjects(objetEnTrop);

        characterService.saveBackPackForCharacter(username, backpack);

        // Essayer d'ajouter l'objet dans le coffre
        characterService.addObjectToCoffre(username, objetEnTrop.getName(), objetEnTrop.getType());

        // Vérification du contenu du coffre
        BackPack backpackAfter = characterService.getBackPackForCharacter(username);
        assertNotNull(backpackAfter, "Le backpack doit être null");
        assertEquals(2, backpackAfter.getObjets().size(), "Le backpack doit contenir un coffre et l'objetEnTrop");
        assertEquals(10, ((be.helha.labos.crystalclash.Object.CoffreDesJoyaux) backpackAfter.getObjets().get(0)).getContenu().size(), "Le coffre doit contenir 10 objets");
    }

    @Test
    @Order(8)
    @DisplayName("Échec - Backpack plein")
    public void testAddObjectToBackpack_Full() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());
        BackPack backpack = characterService.getBackPackForCharacter(username);

        // Remplir le coffre jusqu'à sa capacité max
        for (int i = 1; i <= backpack.getCapaciteMax(); i++) {
            var obj = new be.helha.labos.crystalclash.Object.Weapon("Objet " + i, 10 * i, 1, 5, 5);
            obj.setType("Weapon");
            backpack.AddObjects(obj);
        }
        characterService.saveBackPackForCharacter(username, backpack);

        //crée l'inventaire et mettre objet de trop dedans
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);
        // Ajouter un objet
        var objetEnTrop = new be.helha.labos.crystalclash.Object.Weapon("Objet de trop", 10, 1, 5, 5);
        objetEnTrop.setType("Weapon");
        inventory.ajouterObjet(objetEnTrop);

        inventoryDAO.saveInventoryForUser(username, inventory);

        backpack.AddObjects(objetEnTrop);
        characterService.saveBackPackForCharacter(username, backpack);

        // Vérification du contenu
        BackPack backpackAfter = characterService.getBackPackForCharacter(username);
        Inventory inventeroAfter = inventoryDAO.getInventoryForUser(username);
        assertNotNull(backpackAfter, "Le backpack doit être null");
        assertEquals(5, backpackAfter.getObjets().size(), "Le backpack doit contenir 5 objets");
        assertEquals(1, inventeroAfter.getObjets().size(), "L'inventaire doit contenir 1 objet");

    }


    @Test
    @Order(9)
    @DisplayName("Test récupération d'un equipment")
    public void testGetEquipment() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createEquipmentForCharacter(username, character.getClass().getSimpleName());
        Equipment equipmentGet = characterService.getEquipmentForCharacter(username);
        assertNotNull(equipmentGet, "L'equipement doit être null");
        assertTrue(equipmentGet.getObjets().isEmpty(), "L'equipement doit être vide après la création");
    }

    @Test
    @Order(10)
    @DisplayName("Test d'ajout d'une armure de l'equipement")
    public void testaddInEquipment() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté (tout ce qu'il faut faire pour que le contrôleur fonctionne)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createEquipmentForCharacter(username, character.getClass().getSimpleName());

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Ajouter une armure dans l'inventaire
        var objet = new be.helha.labos.crystalclash.Object.Armor("Armure", 150,15,5,15);
        objet.setType("Armor");
        inventory.ajouterObjet(objet);

        // Sauvegarder l'inventaire
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Ajout de l’armure a l'equipement
        characterService.addArmorToEquipment(username, objet.getName(), objet.getType());

        // Vérification du contenu de l'equipement
        Equipment equipment = characterService.getEquipmentForCharacter(username);
        assertNotNull(equipment, "L'equipement ne doit pas être null");
        assertEquals(1, equipment.getObjets().size(), "L'equipement doit contenir un objet");
        assertEquals("Armure", equipment.getObjets().get(0).getName(), "L'equipement doit être correctement ajouté");
    }

    @Test
    @Order(11)
    @DisplayName("Test suppression d'une armure de l'équipement")
    public void testRemoveArmorFromEquipment() throws Exception {
        String username = "CharacterRemoveArmorUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new Document("username", username));

        // Simuler un utilisateur connecté
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();
        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createEquipmentForCharacter(username, character.getClass().getSimpleName());

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Créer et ajouter une armure à l'inventaire
        var armor = new be.helha.labos.crystalclash.Object.Armor("Armure Supprime", 120, 10, 3, 12);
        armor.setType("Armor");
        inventory.ajouterObjet(armor);
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Ajouter l'armure à l'équipement
        characterService.addArmorToEquipment(username, armor.getName(), armor.getType());

        // Vérification : l'armure est bien ajoutée
        Equipment equipment = characterService.getEquipmentForCharacter(username);
        assertNotNull(equipment);
        assertEquals(1, equipment.getObjets().size());
        assertEquals("Armure Supprime", equipment.getObjets().get(0).getName());

        // Appel de la méthode de suppression
        characterService.removeArmorFromEquipment(username, armor.getName());

        // Récupération après suppression
        Equipment updatedEquipment = characterService.getEquipmentForCharacter(username);
        assertNotNull(updatedEquipment);
        assertTrue(updatedEquipment.getObjets().isEmpty(), "L'équipement devrait être vide après suppression");
    }

    @Test
    @Order(12)
    @DisplayName("Test de la mise à jour de la fiabilité d’un objet dans le backpack")
    public void testUpdateReliabilityInBackPack() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createBackPackForCharacter(username, character.getClass().getSimpleName());

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Ajouter un objet dans l'inventaire
        var objet = new be.helha.labos.crystalclash.Object.Weapon("Épée de test", 100, 5, 5, 50);
        objet.setType("Weapon");
        inventory.ajouterObjet(objet);
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Ajout de l’objet au backpack via le nom et type
        characterService.addObjectToBackPack(username, objet.getName(), objet.getType());

        // Récupérer le backpack et obtenir l'ID réel de l'objet inséré
        BackPack backpack = characterService.getBackPackForCharacter(username);
        assertEquals(1, backpack.getObjets().size(), "Le backpack doit contenir un objet");



        // Mise à jour de la fiabilité en utilisant l'ID correct
        characterService.updateReliabilityInBackPack(username, backpack.getObjets().get(0).getId(), 80);

        // Vérification post-update
        BackPack updatedBackpack = characterService.getBackPackForCharacter(username);
        var updatedObject = updatedBackpack.getObjets().get(0);

        assertEquals("Épée de test", updatedObject.getName(), "Le nom de l’objet doit rester inchangé");
        assertEquals(80, updatedObject.getReliability(), "La fiabilité de l’objet doit avoir été mise à jour à 80");
    }

    @Test
    @Order(13)
    @DisplayName("Test de la mise à jour de la fiabilité d’une armure dans l'equipement'")
    public void testUpdateReliabilityInEquipment() throws Exception {
        String username = "CharacterTestUser";

        var mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").deleteMany(new org.bson.Document("username", username));

        // Simuler un utilisateur connecté
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var character = new be.helha.labos.crystalclash.Characters.Aquaman();

        characterService.saveCharacterForUser(username, character.getClass().getSimpleName());
        characterService.createEquipmentForCharacter(username, character.getClass().getSimpleName());

        // Créer l'inventaire
        inventoryDAO.createInventoryForUser(username);
        Inventory inventory = inventoryDAO.getInventoryForUser(username);

        // Ajouter un objet dans l'inventaire
        var objet = new be.helha.labos.crystalclash.Object.Armor("Armure de test", 100, 5, 5, 50);
        objet.setType("Armor");
        inventory.ajouterObjet(objet);
        inventoryDAO.saveInventoryForUser(username, inventory);

        // Ajout de l’objet a l'equipement via le nom et type
        characterService.addArmorToEquipment(username, objet.getName(), objet.getType());

        // Récupérer l'equipement et obtenir l'ID réel de l'objet inséré
        Equipment equipment = characterService.getEquipmentForCharacter(username);
        assertEquals(1, equipment.getObjets().size(), "L'equipement doit contenir un objet");



        // Mise à jour de la fiabilité en utilisant l'ID correct
        characterService.updateReliabilityInEquipment(username, equipment.getObjets().get(0).getId(), 80);

        // Vérification post-update
        Equipment updatedEquipment = characterService.getEquipmentForCharacter(username);
        var updatedObject = updatedEquipment.getObjets().get(0);

        assertEquals("Armure de test", updatedObject.getName(), "Le nom de l’objet doit rester inchangé");
        assertEquals(80, updatedObject.getReliability(), "La fiabilité de l’objet doit avoir été mise à jour à 80");
    }



}

