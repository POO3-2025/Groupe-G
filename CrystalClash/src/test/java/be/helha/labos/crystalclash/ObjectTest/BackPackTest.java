package be.helha.labos.crystalclash.ObjectTest;

import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.ObjectBase;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class BackPackTest {
    private BackPack backPack;

    @BeforeEach
    void setUp() {
        backPack = new BackPack();
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Order(1)
    @Test
    @DisplayName("Ajout d'un objet dans le BackPacj vide")
    public void testAddObject() {
        ObjectBase obj = new ObjectBase("Epee en bois",15,5,5);
        boolean result = backPack.AddObjects(obj);
        assertTrue(result);
        assertEquals(1, backPack.getObjets().size());
        assertEquals(obj, backPack.getObjets().get(0));
    }

    @Order(2)
    @Test
    @DisplayName("Ne pas ajouter si capacité dépassée")
    public void testAddObjectInFull() {
        //Remplir l invetaire pour le test
        for (int i = 0; i < backPack.getCapaciteMax(); i++) {
            backPack.AddObjects(new ObjectBase("Epee en bois",15,5,5));
        }
        assertEquals(5, backPack.getObjets().size());

        //Et ici on essaie d'en rajouter un
        boolean result = backPack.AddObjects(new ObjectBase("Epee en bois",15,5,5));
        assertFalse(result);
        assertEquals(5, backPack.getObjets().size());
    }

    @Order(3)
    @Test
    @DisplayName("Retirer un objet existant")
    public void testRemoveExistObjet() {
        ObjectBase obj = new ObjectBase("Epee en bois",15,5,5);
        backPack.AddObjects(obj);
        boolean result = backPack.removeObject(obj);
        assertTrue(result);
        assertTrue(backPack.getObjets().isEmpty());
    }

    @Order(4)
    @Test
    @DisplayName("Retirer un objet inexistant")
    public void testRemoveNotExistObject() {
        ObjectBase obj = new ObjectBase("Epee non",15,5,5);
        boolean result = backPack.removeObject(obj);
        assertFalse(result);
    }

    @Order(5)
    @Test
    @DisplayName("Test du getter getObjets()")
    public void testGetObjets() {
        List<ObjectBase> objets = backPack.getObjets();
        assertNotNull(objets);
        assertTrue(objets.isEmpty(), "Le BackPack doit être vide à l'initialisation");
    }

    @Order(6)
    @Test
    @DisplayName("Retour de la capacité maximale du BackPack")
    public void testGetCapaciteMax() {
        int capacite = backPack.getCapaciteMax();
        assertEquals(5, capacite, "La capacité maximale du BackPack doit être 5");
    }
}

