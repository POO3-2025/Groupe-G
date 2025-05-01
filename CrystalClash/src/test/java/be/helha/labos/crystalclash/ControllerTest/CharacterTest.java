package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.server_auth.CrystalClashApplication;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static  org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.mockito.MockedStatic;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = CrystalClashApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class CharacterTest {

    @Autowired
    private MockMvc mockMvc;


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

    }

    //Clean la roulette avant
    @BeforeEach
    public void cleanupMongoRoulette() {
        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        db.getCollection("Characters").deleteMany(new Document()); // supprime tout
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
        stmt.setString(1, "CharacterTestUser");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    /*
     * Ici faut mocker le HttpService pour le token et autre
     *Ouverture de Mock pour simuler le HttpService qui est une méthoe statique et fait appel a un Http d'authentification
     *  mockedStatic.when(() -> et tout le tralala = on dit ce que l'on veut mocker ici c est HttpService.getUserInfo
     *et toi Estelle tu dis ce que tu veux renvoyer donc la c le user créé en haut la (tu retournes juste le json quoi)
     * Juste mit aussi un @Component au dessus public class HttpService de HttpServices
     * Try pour isoler le mock et encapsuler quoi
     *
     * */
    @Test
    @Order(1)
    @DisplayName("Test de la création d'un personnage et recuperation de celui ci")
    @WithMockUser(username = "CharacterTestUser")
    public void testSelectCharacter() throws Exception {
        try (MockedStatic<HttpService> mockedStatic = Mockito.mockStatic(HttpService.class)) {
            // Simuler la réponse du service externe
            mockedStatic.when(() ->
                    HttpService.getUserInfo("CharacterTestUser", "password")
            ).thenReturn("""
                        {
                          "username": "CharacterTestUser",
                          "level": 3,
                          "cristaux": 100,
                          "connected": true
                        }
                    """);

            String requestBody = """
                        {
                            "username": "CharacterTestUser",
                            "characterType": "Elf",
                            "token": "password"
                        }
                    """;

            mockMvc.perform(post("/characters/select")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Personnage sélectionné avec succès !"))
                    .andExpect(jsonPath("$.data").value("Elf"));

            mockMvc.perform(get("/characters/CharacterTestUser")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Personnage récupéré avec succès !"))
                    .andExpect(jsonPath("$.data").value("Elf"));

        }
    }

    @Test
    @Order(2)
    @DisplayName("Test de la recperation d'un backpack")
    @WithMockUser(username = "CharacterTestUser")
    public void testGetBackpack() throws Exception {
        try (MockedStatic<HttpService> mockedStatic = Mockito.mockStatic(HttpService.class)) {
            // Simuler la réponse du service externe
            mockedStatic.when(() ->
                    HttpService.getUserInfo("CharacterTestUser", "password")
            ).thenReturn("""
                        {
                          "username": "CharacterTestUser",
                          "level": 3,
                          "cristaux": 100,
                          "connected": true
                        }
                    """);

            mockMvc.perform(get("/characters/CharacterTestUser/backpack")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Backpack récupéré avec succès"))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

}
