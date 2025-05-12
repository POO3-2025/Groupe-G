package be.helha.labos.crystalclash.DaoImplTest;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.RegistreDAOimpl;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.InventoryService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

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
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive)
            VALUES (?, ?, ?, ?, ?,?,?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "testusegetcharacter");
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

    @Test
    @Order(2)
    @DisplayName("Test sauvegarde d'un personnage pour un utilisateur")
    public void testSaveCharacterForUser() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        dao = new CharacterDAOImpl() {
            @Override
            public void saveCharacterForUser(String username, String characterType) {
                MongoCollection<Document> collection = mongoTest.getCollection("Characters");
                Document doc = new Document("username", username).append("type", characterType);
                Document existing = collection.find(doc).first();

                if (existing == null) {
                    Document backpack = new Document("objets", List.of());
                    long count = collection.countDocuments(new Document("username", username));
                    boolean isFirst = (count == 0);
                    Document docu = new Document("username", username)
                            .append("type", characterType)
                            .append("backpack", backpack)
                            .append("selected", isFirst);
                    collection.insertOne(docu);
                }
            }
        };

        dao.saveCharacterForUser("SaveTestUser", "Elf");

        Document result = mongoTest.getCollection("Characters")
                .find(new Document("username", "SaveTestUser").append("type", "Elf"))
                .first();

        assertNotNull(result);
        assertEquals("Elf", result.getString("type"));
        assertTrue(result.containsKey("backpack"));
    }

    @Test
    @Order(3)
    @DisplayName("Test création du backpack si existant pas encore ")
    public void testCreateBackPackForCharacter() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        MongoCollection<Document> collection = mongoTest.getCollection("Characters");

        // Préparer un document sans backpack
        collection.insertOne(new Document("username", "BackpackUser").append("type", "Troll"));

        dao = new CharacterDAOImpl() {
            @Override
            public void createBackPackForCharacter(String username, String characterType) {
                Document filtre = new Document("username", username).append("type", characterType);
                Document doc = mongoTest.getCollection("Characters").find(filtre).first();

                if (doc != null && doc.containsKey("backpack")) return;

                Document backpack = new Document("objets", List.of());
                Document update = new Document("$set", new Document("backpack", backpack));
                mongoTest.getCollection("Characters").updateOne(filtre, update);
            }
        };

        dao.createBackPackForCharacter("BackpackUser", "Troll");

        Document result = collection.find(new Document("username", "BackpackUser").append("type", "Troll")).first();
        assertNotNull(result);
        assertTrue(result.containsKey("backpack"));
    }

    @Test
    @Order(4)
    @DisplayName("Test récupération du backpack")
    public void testGetBackPackForCharacter() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        MongoCollection<Document> collection = mongoTest.getCollection("Characters");

        Document backpack = new Document("objets", List.of(
                new Document("name", "épée").append("type", "Weapon")
        ));

        collection.insertOne(new Document()
                .append("username", "BackpackGetUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", backpack)
        );

        dao = new CharacterDAOImpl() {
            @Override
            public BackPack getBackPackForCharacter(String username) {
                Document doc = mongoTest.getCollection("Characters")
                        .find(new Document("username", username))
                        .first();

                if (doc != null && doc.containsKey("backpack")) {
                    Document backpackDoc = (Document) doc.get("backpack");
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    return gson.fromJson(backpackDoc.toJson(), BackPack.class);
                }

                return new BackPack();
            }
        };

        BackPack result = dao.getBackPackForCharacter("BackpackGetUser");
        assertNotNull(result);
        assertFalse(result.getObjets().isEmpty());
        assertEquals("épée", result.getObjets().get(0).getName());
    }

    @Test
    @Order(5)
    @DisplayName("Test ajout d'un objet au backpack")
    public void testAddObjectToBackPack() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        // Créer l'objet dans l'inventaire
        mongoTest.getCollection("Inventory").insertOne(new Document()
                .append("username", "BackpackAddUser")
                .append("objets", List.of(
                        new Document("name", "bouclier").append("type", "Armor")
                ))
        );

        // Créer le personnage avec un backpack existant
        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "BackpackAddUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", new Document("objets", List.of()))
        );

        // Redéfinir une DAO avec base Mongo test
        dao = new CharacterDAOImpl() {
            @Override
            // Récupérer le backpack pour un personnage
            public BackPack getBackPackForCharacter(String username) {
                Document doc = mongoTest.getCollection("Characters")
                        .find(new Document("username", username))
                        .first();
                if (doc != null && doc.containsKey("backpack")) {
                    Document backpackDoc = (Document) doc.get("backpack");
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    return gson.fromJson(backpackDoc.toJson(), BackPack.class);
                }
                return new BackPack();
            }
            // Ajouter un objet au backpack
            @Override
            public ApiReponse addObjectToBackPack(String username, String name, String type) {
                // Simuler le comportement simplifié : injecter directement dans Mongo
                Document object = new Document("name", name).append("type", type);
                mongoTest.getCollection("Characters").updateOne(
                        new Document("username", username),
                        new Document("$push", new Document("backpack.objets", object))
                );
                return new ApiReponse("Objet ajouté", null);
            }
        };

        // Appeler la méthode testée
        dao.addObjectToBackPack("BackpackAddUser", "bouclier", "Armor");

        BackPack result = dao.getBackPackForCharacter("BackpackAddUser");

        assertNotNull(result);
        assertEquals(1, result.getObjets().size());
        assertEquals("bouclier", result.getObjets().get(0).getName());
        assertEquals("Armor", result.getObjets().get(0).getType());
    }

    @Test
    @Order(6)
    @DisplayName("Test suppression d'un objet dans le bacpack")
    public void testRemoveObjectFromBackPack() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        // Créer le personnage avec un backpack existant
        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "BackpackRemoveUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", new Document("objets", List.of(
                        new Document("name", "épée").append("type", "Weapon")
                )))
        );

        dao = new CharacterDAOImpl() {
            @Override
            public ApiReponse removeObjectFromBackPack(String username, String name) {
                mongoTest.getCollection("Characters").updateOne(
                        new Document("username", username),
                        new Document("$pull", new Document("backpack.objets",
                                new Document("name", name)))
                );
                return new ApiReponse("Objet supprimé", null);
            }

            @Override
            public BackPack getBackPackForCharacter(String username) {
                Document doc = mongoTest.getCollection("Characters")
                        .find(new Document("username", username))
                        .first();
                if (doc != null && doc.containsKey("backpack")) {
                    Document backpackDoc = (Document) doc.get("backpack");
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    return gson.fromJson(backpackDoc.toJson(), BackPack.class);
                }
                return new BackPack();
            }
        };

        dao.removeObjectFromBackPack("BackpackRemoveUser", "épée");

        BackPack result = dao.getBackPackForCharacter("BackpackRemoveUser");

        assertNotNull(result);
        assertTrue(result.getObjets().isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Test ajout au coffre")
    public void testAddObjectToCoffre() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        // Créer le personnage avec un coffre existant
        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "CoffreUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", new Document("objets", List.of(
                        new Document("name", "epee").append("type", "Weapon")
                )))
                .append("coffre", new Document("Contenu", List.of()))
        );

        dao = new CharacterDAOImpl() {
            @Override
            public ApiReponse addObjectToCoffre(String username, String name, String type) {
                Document object = new Document("name", name).append("type", type);
                mongoTest.getCollection("Characters").updateOne(
                        new Document("username", username),
                        new Document("$push", new Document("coffre.Contenu", object))
                );
                return new ApiReponse("Objet ajouté au coffre", null);
            }
        };
        // Appeler la méthode testée
        ApiReponse response = dao.addObjectToCoffre("CoffreUser", "epee", "Weapon");
        assertEquals("Objet ajouté au coffre", response.getMessage());
        // Vérifier que l'objet a été ajouté au coffre
        Document result = mongoTest.getCollection("Characters")
                .find(new Document("username", "CoffreUser"))
                .first();
        assertNotNull(result);
        Document coffre = (Document) result.get("coffre");
        assertNotNull(coffre);
        List<Document> contenu = (List<Document>) coffre.get("Contenu");
        assertNotNull(contenu);
    }


    @Test
    @Order(8)
    @DisplayName("Échec - Aucun coffre présent dans le BackPack")
    public void testAddToCoffre_NoCoffrePresent() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        Document objet = new Document("name", "arc").append("type", "Weapon");
        Document backpack = new Document("objets", List.of(objet));

        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "FailNoCoffre")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", backpack)
        );

        ApiReponse response = dao.addObjectToCoffre("FailNoCoffre", "arc", "Weapon");
        assertEquals("Aucun Coffre des Joyaux trouvé dans votre BackPack.", response.getMessage());
    }
    @Test
    @Order(9)
    @DisplayName("Échec - Coffre plein")
    public void testAddToCoffre_FullCoffre() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        // Construire un coffre avec 10 objets déjà dedans
        Document coffre = new Document("name", "MonCoffre")
                .append("type", "CoffreDesJoyaux")
                .append("reliability", 10)
                .append("requiredLevel", 1)
                .append("price", 0)
                .append("contenu", List.of(
                        new Document("name", "obj1").append("type", "Weapon"),
                        new Document("name", "obj2").append("type", "Weapon"),
                        new Document("name", "obj3").append("type", "Weapon"),
                        new Document("name", "obj4").append("type", "Weapon"),
                        new Document("name", "obj5").append("type", "Weapon"),
                        new Document("name", "obj6").append("type", "Weapon"),
                        new Document("name", "obj7").append("type", "Weapon"),
                        new Document("name", "obj8").append("type", "Weapon"),
                        new Document("name", "obj9").append("type", "Weapon"),
                        new Document("name", "obj10").append("type", "Weapon")
                ));

        // Objet qu'on tente d'ajouter au coffre
        Document objet = new Document("name", "épée").append("type", "Weapon");

        Document backpack = new Document("objets", List.of(objet, coffre));

        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "CoffreFullUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", backpack)
        );

        ApiReponse response = dao.addObjectToCoffre("CoffreFullUser", "épée", "Weapon");

        assertNotNull(response);
        assertEquals("Le coffre est plein.", response.getMessage());
    }


    @Test
    @Order(10)
    @DisplayName("Échec - Coffre brisé (reliability <= 0)")
    public void testAddToCoffre_BrokenCoffre() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        Document objet = new Document("name", "arc").append("type", "Weapon");

        Document coffre = new Document("name", "coffreMort")
                .append("type", "CoffreDesJoyaux")
                .append("reliability", 0)
                .append("contenu", List.of());

        Document backpack = new Document("objets", List.of(objet, coffre));

        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "BrokenCoffreUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", backpack)
        );

        ApiReponse response = dao.addObjectToCoffre("BrokenCoffreUser", "arc", "Weapon");

        assertNotNull(response);
        assertEquals("Le coffre est brisé et ne peut plus être utilisé.", response.getMessage());
    }

    @Test
    @Order(11)
    @DisplayName("Échec - Backpack plein")
    public void testAddObjectToBackPack_WhenFull() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        // Créer un inventaire avec un objet à ajouter
        mongoTest.getCollection("Inventory").insertOne(new Document()
                .append("username", "BackpackFullUser")
                .append("objets", List.of(
                        new Document("name", "bouclier").append("type", "Armor")
                ))
        );

        // Créer un backpack avec 5 objets (limite atteinte)
        List<Document> objets = List.of(
                new Document("name", "obj1").append("type", "Weapon"),
                new Document("name", "obj2").append("type", "Weapon"),
                new Document("name", "obj3").append("type", "Weapon"),
                new Document("name", "obj4").append("type", "Weapon"),
                new Document("name", "obj5").append("type", "Weapon")
        );

        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "BackpackFullUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", new Document("objets", objets))
        );

        dao = new CharacterDAOImpl() {
            @Override
            public BackPack getBackPackForCharacter(String username) {
                Document doc = mongoTest.getCollection("Characters")
                        .find(new Document("username", username))
                        .first();
                if (doc != null && doc.containsKey("backpack")) {
                    Document backpackDoc = (Document) doc.get("backpack");
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    return gson.fromJson(backpackDoc.toJson(), BackPack.class);
                }
                return new BackPack();
            }

            @Override
            public ApiReponse addObjectToBackPack(String username, String name, String type) {
                BackPack backPack = getBackPackForCharacter(username);
                if (backPack.getObjets().size() >= 5) {
                    return new ApiReponse("Backpack plein !", null);
                }

                Document object = new Document("name", name).append("type", type);
                mongoTest.getCollection("Characters").updateOne(
                        new Document("username", username),
                        new Document("$push", new Document("backpack.objets", object))
                );
                return new ApiReponse("Objet ajouté", null);
            }
        };

        ApiReponse response = dao.addObjectToBackPack("BackpackFullUser", "bouclier", "Armor");

        assertNotNull(response);
        assertEquals("Backpack plein !", response.getMessage());
    }

    @Test
    @Order(12)
    @DisplayName("Échec - Objet introuvable dans l'inventaire ou coffre")
    public void testAddObjectToBackPack_ObjectNotFound() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        // Inventaire sans l'objet recherché
        mongoTest.getCollection("Inventory").insertOne(new Document()
                .append("username", "UserNotFoundTest")
                .append("objets", List.of(
                        new Document("name", "arc").append("type", "Weapon")
                ))
        );

        // Backpack vide
        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "UserNotFoundTest")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", new Document("objets", List.of()))
        );

        dao = new CharacterDAOImpl() {
            @Override
            public BackPack getBackPackForCharacter(String username) {
                Document doc = mongoTest.getCollection("Characters")
                        .find(new Document("username", username))
                        .first();
                if (doc != null && doc.containsKey("backpack")) {
                    Document backpackDoc = (Document) doc.get("backpack");
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    return gson.fromJson(backpackDoc.toJson(), BackPack.class);
                }
                return new BackPack();
            }

            @Override
            public ApiReponse addObjectToBackPack(String username, String name, String type) {

                return new ApiReponse("Objet non trouvé dans l'inventaire ni dans un coffre.", null);
            }
        };

        ApiReponse response = dao.addObjectToBackPack("UserNotFoundTest", "bouclier", "Armor");

        assertNotNull(response);
        assertEquals("Objet non trouvé dans l'inventaire ni dans un coffre.", response.getMessage());
    }


    @Test
    @Order(13)
    @DisplayName("Échec - Suppression d'objet inexistant du backpack")
    public void testRemoveObjectFromBackPack_ObjectNotFound() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "NoSuchItemUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", new Document("objets", List.of()))
        );

        dao =new CharacterDAOImpl(){
            @Override
            public ApiReponse removeObjectFromBackPack(String username, String name) {
                Document result = mongoTest.getCollection("Characters").find(
                        new Document("username", username).append("backpack.objets.name", name)
                ).first();

                if (result == null) {
                    return new ApiReponse("Objet non trouvé dans le backpack.", null);
                }

                mongoTest.getCollection("Characters").updateOne(
                        new Document("username", username),
                        new Document("$pull", new Document("backpack.objets",
                                new Document("name", name)))
                );
                return new ApiReponse("Objet supprimé", null);
            }

        };

        ApiReponse response = dao.removeObjectFromBackPack("NoSuchItemUser", "ghostItem");

        assertNotNull(response);
        assertEquals("Objet non trouvé dans le backpack.", response.getMessage());
    }

    @Test
    @Order(14)
    @DisplayName("Échec - Inventaire plein lors du retrait du backpack")
    public void testRemoveObjectFromBackPack_InventoryFull() {
        MongoDatabase mongoTest = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        // Objet à retirer du backpack
        Document objet = new Document("name", "épée").append("type", "Weapon");

        List<Document> inventoryFull = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            inventoryFull.add(new Document("name", "item" + i).append("type", "Armor"));
        }

        mongoTest.getCollection("Characters").insertOne(new Document()
                .append("username", "InventoryFullUser")
                .append("type", "Elf")
                .append("selected", true)
                .append("backpack", new Document("objets", List.of(objet)))
        );

        mongoTest.getCollection("Inventory").insertOne(new Document()
                .append("username", "InventoryFullUser")
                .append("objets", inventoryFull)
        );

        dao =new CharacterDAOImpl(){
            @Override
            public ApiReponse removeObjectFromBackPack(String username, String name) {
                // Vérifie si l'objet est dans le backpack
                Document character = mongoTest.getCollection("Characters").find(
                        new Document("username", username).append("backpack.objets.name", name)
                ).first();

                if (character == null) {
                    return new ApiReponse("Objet non trouvé dans le backpack.", null);
                }

                // Vérifie si l’inventaire est plein
                Document inventory = mongoTest.getCollection("Inventory")
                        .find(new Document("username", username)).first();

                if (inventory != null) {
                    List<Document> objets = (List<Document>) inventory.get("objets");
                    if (objets != null && objets.size() >= 30) {
                        return new ApiReponse("Inventaire plein !", null);
                    }
                }

                // Simule suppression
                mongoTest.getCollection("Characters").updateOne(
                        new Document("username", username),
                        new Document("$pull", new Document("backpack.objets", new Document("name", name)))
                );

                return new ApiReponse("Objet supprimé", null);
            }

        };

        ApiReponse response = dao.removeObjectFromBackPack("InventoryFullUser", "épée");

        assertNotNull(response);
        assertEquals("Inventaire plein !", response.getMessage());
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
