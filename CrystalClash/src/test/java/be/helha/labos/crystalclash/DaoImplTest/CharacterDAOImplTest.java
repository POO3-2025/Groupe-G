package be.helha.labos.crystalclash.DaoImplTest;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.RegistreDAOimpl;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.ObjectBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

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
    //ma souler
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
