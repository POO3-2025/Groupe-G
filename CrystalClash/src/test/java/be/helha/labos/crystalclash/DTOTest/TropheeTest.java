package be.helha.labos.crystalclash.DTOTest;

import be.helha.labos.crystalclash.DTO.Trophee;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TropheeTest {

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Order(1)
    @Test
    @DisplayName("Test constructeur avec paramètre et getter")
    public void testConstructeurEtGetters() {
        Trophee trophee = new Trophee("Vainqueur", "Gagner un combat", false);

        assertEquals("Vainqueur", trophee.getNom());
        assertEquals("Gagner un combat", trophee.getDescription());
        assertFalse(trophee.isObtenu());
    }

    @Order(2)
    @Test
    @DisplayName("Test constructeur vide + setter")
    public void testConstructeurVideEtSetters() {
        Trophee trophee = new Trophee("", "", false);
        trophee.setNom("Champion");
        trophee.setDescription("Gagner le tournoi");
        trophee.setObtenu(true);

        assertEquals("Champion", trophee.getNom());
        assertEquals("Gagner le tournoi", trophee.getDescription());
        assertTrue(trophee.isObtenu());
    }

    @Order(3)
    @Test
    @DisplayName("Test valeur vide")
    public void testValeurVide() {
        Trophee trophee = new Trophee("", "", false);

        assertEquals("", trophee.getNom());
        assertEquals("", trophee.getDescription());
        assertFalse(trophee.isObtenu());
    }

    @Order(4)
    @Test
    @DisplayName("Test Setters")
    public void testSetters() {
        Trophee trophee = new Trophee("A", "B", false);
        trophee.setNom("Explorateur");
        trophee.setDescription("Visiter 10 zones");
        trophee.setObtenu(true);

        assertEquals("Explorateur", trophee.getNom());
        assertEquals("Visiter 10 zones", trophee.getDescription());
        assertTrue(trophee.isObtenu());
    }

    @Order(5)
    @Test
    @DisplayName("Test debloquer")
    public void testDebloquer() {
        Trophee trophee = new Trophee("Débloqueur", "Tester débloquer", false);
        trophee.debloquer();
        assertTrue(trophee.isObtenu());
    }

}
