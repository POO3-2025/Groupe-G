package be.helha.labos.crystalclash.ObjectTest;

import be.helha.labos.crystalclash.Object.Armor;
import be.helha.labos.crystalclash.Object.Weapon;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class WeaponTest {

    private Weapon weapon;

    @BeforeEach
    void setup(){
        weapon = new Weapon("fusil", 150, 7, 5, 10);
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    @Test
    @Order(1)
    @DisplayName("Test des getters Weapon")
    public void gettersArmor() {
        assertEquals("fusil",weapon.getName());
        assertEquals(150,weapon.getPrice());
        assertEquals(7,weapon.getRequiredLevel());
        assertEquals(5,weapon.getReliability());
        assertEquals(10,weapon.getDamage());
    }

    @Test
    @Order(2)
    @DisplayName("Test méthode use > 0")
    public void testUsedWhenUsable(){
        String result = weapon.use();
        assertEquals("The weapon deal 10 damage", result);
        assertEquals(4,weapon.getReliability());
    }

    @Test
    @Order(3)
    @DisplayName("Test méthode use == 0")
    public void testUsedWhenNotUsable(){
        weapon.setReliability(0);
        String result = weapon.use();
        assertEquals("The weapon is broken", result);
    }

    @Test
    @Order(4)
    @DisplayName("Test du ToString")
    public void testToString(){
        assertTrue(weapon.toString().contains("Dégâts : 10"));
    }

    @Test
    @Order(5)
    @DisplayName("Test de getDetails() personnalisé")
    void testGetDetails() {
        String details = weapon.getDetails();
        assertTrue(details.contains("Dégâts : 10"));
    }
}


