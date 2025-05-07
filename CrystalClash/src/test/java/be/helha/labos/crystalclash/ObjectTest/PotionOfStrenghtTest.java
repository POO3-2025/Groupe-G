
package be.helha.labos.crystalclash.ObjectTest;

import be.helha.labos.crystalclash.Object.PotionOfStrenght;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PotionOfStrenghtTest {

    private PotionOfStrenght potionOfStrenght;

    @BeforeEach
    void setup(){
        potionOfStrenght = new PotionOfStrenght("venin dombre", 25, 2, 5);
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    @DisplayName("Test des getters potionOfStrenght")
    public void gettersArmor() {
        assertEquals("venin dombre",potionOfStrenght.getName());
        assertEquals(25,potionOfStrenght.getPrice());
        assertEquals(2,potionOfStrenght.getRequiredLevel());
        assertEquals(5,potionOfStrenght.getBonusATK());
    }

    @Test
    @Order(2)
    @DisplayName("Test méthode use > 0")
    public void testUsedWhenUsable(){
        String result = potionOfStrenght.use();
        assertEquals("You won 5 in ATK", result);
        assertEquals(0,potionOfStrenght.getReliability());
    }

    @Test
    @Order(3)
    @DisplayName("Test méthode use == 0")
    public void testUsedWhenNotUsable(){
        potionOfStrenght.setReliability(0);
        String result = potionOfStrenght.use();
        assertEquals("Potion already used", result);
    }

    @Test
    @Order(4)
    @DisplayName("Test du ToString")
    public void testToString(){
        assertTrue(potionOfStrenght.toString().contains("Dégâts : 5"));
    }

    @Test
    @Order(5)
    @DisplayName("Test de getDetails() personnalisé")
    void testGetDetails() {
        String details = potionOfStrenght.getDetails();
        assertTrue(details.contains("BonusATK : 5"));
    }
}
