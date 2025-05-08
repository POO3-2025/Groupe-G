package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.server_auth.CrystalClashApplication;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CrystalClashApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CharacterService characterService;
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

        var mysqlProductionConfig = db.getAsJsonObject("mysqlproduction");
        var mysqlTestConfig = db.getAsJsonObject("mysqltest");

        mysqlProductionConfig.getAsJsonObject("BDCredentials")
                .entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    mysqlProductionConfig.getAsJsonObject("BDCredentials")
                            .add(key, mysqlTestConfig.getAsJsonObject("BDCredentials").get(key));
                });
    }

    @BeforeEach
    public void ensureUserExists_and_mongo() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "UserControllerTestUser");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        //Insertion pour le perso du user .
        MongoDatabase mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        mongo.getCollection("Characters").insertOne(new Document("username", "UserControllerTestUser")
                .append("type", "troll")
                .append("selected", true));

    }

    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }



    /*
    * Test recup le user.
    * */
    @Test
    @Order(1)
    @DisplayName("Test récupération d’un utilisateur existant")
    @WithMockUser(username = "UserControllerTestUser")
    public void RecupUserExistant() throws Exception {
        mockMvc.perform(get("/user/UserControllerTestUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("UserControllerTestUser"))
                .andExpect(jsonPath("$.level").value(5))
                .andExpect(jsonPath("$.cristaux").value(100))
                .andExpect(jsonPath("$.selectedCharacter").value("troll"));

    }


    /*
     * Test si un user est bien inexistant
     * */
    @Test
    @Order(2)
    @DisplayName("Test récupération d’un utilisateur inexistant")
    @WithMockUser(username = "InexistantUser")
    public void testGetUserByUsername_notFound() throws Exception {
        mockMvc.perform(get("/user/InexistantUser"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }

    /*
    * Test avec un token bidon
    * */
    @Test
    @Order(3)
    @DisplayName("Test accès refusé avec token invalide")
    public void testAccessDeniedWithInvalidToken() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user/UserControllerTestUser"))
                .header("Authorization", "Bearer faketoken")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(403, response.statusCode());
    }

   /* @AfterAll
    public void resetMySQLUsers_AND_Mongo() throws Exception {
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("DELETE FROM users");
        stmt.executeUpdate();
        stmt.close();
        conn.close();
        System.out.println("Tous les utilisateurs MySQL ont été supprimés.");

        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");

        db.getCollection("Inventory").deleteMany(new Document());
        db.getCollection("Characters").deleteMany(new Document());
        // Ajoute d'autres collections si besoin

        System.out.println("Toutes les données Mongo ont été supprimées.");
    }
*/
}


