package be.helha.labos.crystalclash.DTOTest;

import be.helha.labos.crystalclash.DTO.LogoutRequest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogoutRequestTest {

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Order(1)
    @Test
    @DisplayName("Test constructeur avec paramètre et getter")
    public void testConstructorAndGetters() {
        LogoutRequest request = new LogoutRequest("testUser");
        assertEquals("testUser", request.getUsername(), "Le getter doit retourner le nom d'utilisateur fourni");
    }

    @Order(2)
    @Test
    @DisplayName("Test constructeur vide + setter")
    public void testSetUsername() {
        LogoutRequest request = new LogoutRequest();
        request.setUsername("anotherUser");
        assertEquals("anotherUser", request.getUsername(), "Le setter doit mettre à jour le nom d'utilisateur");
    }

    @Order(3)
    @Test
    @DisplayName("Test valeur vide")
    public void testEmptyUsername() {
        LogoutRequest request = new LogoutRequest("");
        assertEquals("", request.getUsername(), "Le nom d'utilisateur vide doit être accepté tel quel");
    }

    @Order(4)
    @Test
    @DisplayName("Test valeur nulle")
    public void testNullUsername() {
        LogoutRequest request = new LogoutRequest(null);
        assertNull(request.getUsername(), "Le nom d'utilisateur null doit être retourné comme null");
    }
}
