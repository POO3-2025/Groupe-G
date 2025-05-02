package be.helha.labos.crystalclash.ObjectTest;

import be.helha.labos.crystalclash.Object.Armor;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArmorTest {

    private Armor armor;

    @BeforeEach
    void setup(){
        armor = new Armor("Armure légère", 50, 2, 3, 20);
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    @DisplayName("Test des getters Armor")
    public void gettersArmor() {
        assertEquals("Armure légère",armor.getName());
        assertEquals(50,armor.getPrice());
        assertEquals(2,armor.getRequiredLevel());
        assertEquals(3,armor.getReliability());
        assertEquals(20,armor.getBonusPV());
    }

    @Test
    @Order(2)
    @DisplayName("Test méthode use > 0")
    public void testUsedWhenUsable(){
        String result = armor.use();
        assertTrue(result.contains("+20Pv"));
        assertEquals(2,armor.getReliability());
    }

    @Test
    @Order(3)
    @DisplayName("Test méthode use == 0")
    public void testUsedWhenNotUsable(){
        armor.setReliability(0);
        String result = armor.use();
        assertEquals("The Armor worn", result);
    }

    @Test
    @Order(4)
    @DisplayName("Test du ToString")
    public void testToString(){
        assertTrue(armor.toString().contains("Dégâts : 20"));
    }

    @Test
    @Order(5)
    @DisplayName("Test de getDetails() personnalisé")
    void testGetDetails() {
        String details = armor.getDetails();
        assertTrue(details.contains("Défense : 20"));
    }

}
