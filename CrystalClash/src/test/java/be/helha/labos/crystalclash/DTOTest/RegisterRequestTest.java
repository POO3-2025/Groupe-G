package be.helha.labos.crystalclash.DTOTest;


import be.helha.labos.crystalclash.DTO.RegisterRequest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
public class RegisterRequestTest {

    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    @Order(1)
    @Test
    @DisplayName("Test Constructeur et Getters")
    void testConstructorAndGetters() {
            RegisterRequest req = new RegisterRequest("toto", "tata");

            assertEquals("toto", req.getUsername());
            assertEquals("tata", req.getPassword());
        }

    @Order(2)
    @Test
    @DisplayName("Test setters")
    void testSetters() {
            RegisterRequest req = new RegisterRequest("user", "pass");

            req.setUsername("newUser");
            req.setPassword("newPass");

            assertEquals("newUser", req.getUsername());
            assertEquals("newPass", req.getPassword());
        }

    @Order(3)
    @Test
    @DisplayName("Test valuers vides")
    void testEmptyValues() {
            RegisterRequest req = new RegisterRequest("", "");

            assertEquals("", req.getUsername());
            assertEquals("", req.getPassword());
        }

        @Order(4)
        @Test
        @DisplayName("Test valuers nulles")
        void testNullValues() {
            RegisterRequest req = new RegisterRequest(null, null);

            assertNull(req.getUsername());
            assertNull(req.getPassword());
        }
    }


