
package be.helha.labos.crystalclash.InventoryTest;

import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Order(1)
    @Test
    @DisplayName("Ajout d'un objet dans l'inventaire vide")
    public void testAjouterObjet() {
        ObjectBase obj = new ObjectBase("Epee en bois",15,5,5);
        boolean result = inventory.ajouterObjet(obj);
        assertTrue(result);
        assertEquals(1, inventory.getObjets().size());
        assertEquals(obj, inventory.getObjets().get(0));
    }

    @Order(2)
    @Test
    @DisplayName("Ne pas ajouter si capacité dépassée")
    public void testAjouterObjetQuandPlein() {
        //Remplir l invetaire pour le test
        for (int i = 0; i < inventory.getCapaciteMax(); i++) {
            inventory.ajouterObjet(new ObjectBase("Epee en bois",15,5,5));
        }
        assertEquals(30, inventory.getObjets().size());

        //Et ici on essaie d'en rajouter un
        boolean result = inventory.ajouterObjet(new ObjectBase("Epee en bois",15,5,5));
        assertFalse(result);
        assertEquals(30, inventory.getObjets().size());
    }

    @Order(3)
    @Test
    @DisplayName("Retirer un objet existant")
    public void testRetirerObjetExistant() {
        ObjectBase obj = new ObjectBase("Epee en bois",15,5,5);
        inventory.ajouterObjet(obj);
        boolean result = inventory.retirerObjet(obj);
        assertTrue(result);
        assertTrue(inventory.getObjets().isEmpty());
    }

    @Order(4)
    @Test
    @DisplayName("Retirer un objet inexistant")
    public void testRetirerObjetInexistant() {
        ObjectBase obj = new ObjectBase("Epee non",15,5,5);
        boolean result = inventory.retirerObjet(obj);
        assertFalse(result);
    }

    @Order(5)
    @Test
    @DisplayName("Test setUsername et getUsername")
    public void testSetUsername() {
        inventory.setUsername("CelioTests");
        assertEquals("CelioTests", inventory.getUsername());
    }

    @Order(6)
    @Test
    @DisplayName("Test setObjets")
    public void testSetObjets() {
        List<ObjectBase> list = new ArrayList<>();
        list.add(new ObjectBase("Epee en bois",15,5,5));
        inventory.setObjets(list);
        assertEquals(1, inventory.getObjets().size());
    }

    @Order(5)
    @Test
    @DisplayName("Test du getter getObjets()")
    public void testGetObjets() {
        List<ObjectBase> objets = inventory.getObjets();
        assertNotNull(objets);
        assertTrue(objets.isEmpty(), "L'inventaire doit être vide à l'initialisation");
    }

    @Order(6)
    @Test
    @DisplayName("Retour de la capacité maximale de l'inventaire")
    public void testGetCapaciteMax() {
        int capacite = inventory.getCapaciteMax();
        assertEquals(30, capacite, "La capacité maximale de l'inventaire doit être 5");
    }
}
