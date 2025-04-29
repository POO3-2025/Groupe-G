package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DTO.RegisterRequest;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.RegistreService;
import be.helha.labos.crystalclash.server_auth.AuthResponse;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import be.helha.labos.crystalclash.DAOImpl.RegistreDAOimpl;
import be.helha.labos.crystalclash.Controller.RegistreController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegistreControllerTest {

    private RegistreController registreController;


    @BeforeEach
    public void setUp() throws Exception {
        registreController = new RegistreController();

        // Forcer ConfigManager à utiliser mysqltest au lieu de mysqlproduction
        // le but est de modifier temporairement le fichier config.json en mémoire
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");
        var mysqlProductionConfig = db.getAsJsonObject("mysqlproduction");
        var mysqlTestConfig = db.getAsJsonObject("mysqltest");
        //  copie les valeurs de mysqltest dans mysqlproduction
        mysqlProductionConfig.getAsJsonObject("BDCredentials")
            .entrySet()
            .forEach(entry -> {
                String key = entry.getKey();
                mysqlProductionConfig.getAsJsonObject("BDCredentials")
                    .add(key, mysqlTestConfig.getAsJsonObject("BDCredentials").get(key));
            });

        //Forcer mongo aussi a prednre la db de test
        var mongoProductionConfig = db.getAsJsonObject("MongoDBProduction");
        var mongoTestConfig = db.getAsJsonObject("MongoDBTest");

        mongoProductionConfig.getAsJsonObject("BDCredentials")
            .entrySet()
            .forEach(entry -> {
                String key = entry.getKey();
                mongoProductionConfig.getAsJsonObject("BDCredentials")
                    .add(key, mongoTestConfig.getAsJsonObject("BDCredentials").get(key));
            });

        // Maintenant même si DAO fait mysqlproduction, il ira sur mysqltest vraiment
        RegistreDAOimpl registreDAO = new RegistreDAOimpl();
        InventoryDAOImpl inventoryDAO = new InventoryDAOImpl(); // utilise MongoDBTest par défaut

        // Création services
        RegistreService registreService = new RegistreService(registreDAO);
        InventoryService inventoryService = new InventoryService(inventoryDAO);

        // Encoder
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        // Injection dans le controller
        registreController.setPasswordEncoder(encoder);
        registreController.setRegistreService(registreService);
        registreController.setInventoryService(inventoryService);
    }



    @Test
    @Order(1)
    public void testRegisterUser_success() {
        RegisterRequest request = new RegisterRequest("testuser3", "testpass");

        ResponseEntity<?> response = registreController.registerUser(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("Inscription réussie !", authResponse.getMessage());

        //Vérifier que l'inventaire MongoDBTest a été créé
        InventoryDAOImpl inventoryDAO = new InventoryDAOImpl(); // utilise MongoDBTest
        assertNotNull(inventoryDAO.getInventoryForUser("testuser1"), "L'inventaire du user doit être créé dans MongoDBTest !");
    }


}
