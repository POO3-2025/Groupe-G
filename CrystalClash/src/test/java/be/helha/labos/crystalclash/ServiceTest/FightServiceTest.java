package be.helha.labos.crystalclash.ServiceTest;


import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.DAO.FightDAO;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DAO.UserDAO;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.FightDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserDAOImpl;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Object.Weapon;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.FightService;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNull;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

public class FightServiceTest {

    private FightService fightService;


    private final String user1 = "userTes1";
    private final String user2 = "userTes2";


    @BeforeEach
        public void setUp() throws Exception {
            var config = ConfigManager.getInstance().getConfig();
            var db = config.getAsJsonObject("db");

            var mysqlProductionConfig = db.getAsJsonObject("mysqlproduction");
            var mysqlTestConfig = db.getAsJsonObject("mysqltest");

            mysqlProductionConfig.getAsJsonObject("BDCredentials")
                .entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    mysqlProductionConfig.getAsJsonObject("BDCredentials")
                        .add(key, mysqlTestConfig.getAsJsonObject("BDCredentials").get(key));
                });

            var mongoProductionConfig = db.getAsJsonObject("MongoDBProduction");
            var mongoTestConfig = db.getAsJsonObject("MongoDBTest");

            //Accede aux credential de mongo
            mongoProductionConfig.getAsJsonObject("BDCredentials")
                .entrySet()//Récupe les clées, valeurs
                .forEach(entry -> { //Pour chaque clées
                    String key = entry.getKey();  //ça récup la clé (hostname,...)
                    mongoProductionConfig.getAsJsonObject("BDCredentials")
                        .add(key, mongoTestConfig.getAsJsonObject("BDCredentials").get(key)); //Et ça remplace la valeur prod pas celle de test
                });

            //Implementation Dao manuellement
        CharacterDAO characterDAO = new CharacterDAOImpl();
        UserDAO userDAO = new UserDAOImpl();
        InventoryDAO inventoryDAO = new InventoryDAOImpl();
        FightDAO fightDAO = new FightDAOImpl();

        //Service ici
        CharacterService characterService = new CharacterService(characterDAO);
        UserService userService = new UserService(userDAO);
        InventoryService inventoryService = new InventoryService(inventoryDAO);
        inventoryService.setUserService(userService);

        fightService = new FightService(fightDAO);
        fightService.setCharacterService(characterService);
        fightService.setUserService(userService);
        fightService.setInventoryService(inventoryService);


        }

    /*
     * Insertion d un user test pour le test userExist
     * */
    @BeforeEach
    public void ensureUserExists() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive	)
          VALUES (?, ?, ?, ?, ?, ?, ?,?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "userTes1");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.setInt(6, 0); // valeur par défaut pour 'gagner'
        stmt.setInt(7, 0); // valeur par défaut pour 'perdu'
        stmt.setInt(8, 0);
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        var conn2 = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt2 = conn2.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive)
    VALUES (?, ?, ?, ?, ?, ?, ?,?)
ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt2.setString(1, "userTes2");
        stmt2.setString(2, "password");
        stmt2.setInt(3, 5);
        stmt2.setInt(4, 100);
        stmt2.setBoolean(5, false);
        stmt2.setInt(6, 0); // valeur par défaut pour 'gagner'
        stmt2.setInt(7, 0); // valeur par défaut pour 'perdu'
        stmt2.setInt(8, 0);
        stmt2.executeUpdate();
        stmt2.close();
        conn2.close();
    }

    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    @Test
    @Order(1)
    @DisplayName("Test créer un combat et attaquer")
public void TestCreateFightAndAttack(){

        //Création 2 persos
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        //PAs objet dans le bp
        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        //Creation du combat
        fightService.createCombat(user1,user2,p1,p2,bp1,bp2);

        //Vérif combat enregistré
        StateCombat combat = fightService.getCombat(user1);
        assertNotNull(combat, "le combat devrait etre présent");
        assertEquals(user1, combat.getPlayerNow(), "Le joueur 1 devarait commencer");

        int pvInitialOppenent = combat.getPv(user2);

        //Attaque normal du user1
        fightService.HandleAttach(user1,"normal");

        //Assert pv user2 diminués
        StateCombat combat2 = fightService.getCombat(user2);
        int pvAfterAttack = combat2.getPv(user2);

        assertTrue(pvAfterAttack < pvInitialOppenent, "Les PV de l'adversaire doivent avoir baissé.");
        assertEquals(user2, combat2.getPlayerNow(), "C'est maintenant au joueur 2 de jouer.");
    }

    @Test
    @Order(2)
    @DisplayName("Test utiliser un objet")
        public void TestUseObject() throws Exception {

        //Création 2 persos
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        Weapon weapon = new Weapon("Epee en bois",23,1,2,5);
        weapon.setId("test");

        List<ObjectBase> bp1 = List.of(weapon);
        List<ObjectBase> bp2 = List.of();

        //Cree combat
        fightService.createCombat(user1,user2,p1,p2,bp1,bp2);
        fightService.useObject(user1, "test");

        //CHercher etat combat pour user2
        StateCombat combat = fightService.getCombat(user2);
        assertTrue(combat.getPv(user2) < p2.getPV()); //verif si pv bien diminués

        }

    @Test
    @Order(3)
    @DisplayName("Test Forfait d'un joueur")
    public void TestFormfaitFinoshFight() throws Exception {

        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);
        fightService.forfait(user1);

        StateCombat combat = fightService.getCombat(user1);
        assertNull(combat);
    }

    @Test
    @Order(4)
    @DisplayName("Test attaque spéciale après nombre de tours requis")
    public void TestSpecialAttackAfterRequiredTurns() {

        // Création des personnages avec attaques configurées
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        // forcer p1 à avoir assez de tours pour faire une attaque spéciale
        p1.CompteurAttack(p1.getRestrictionAttackSpecial()); // simu pour attack psecial ok

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        // Création du combat
        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // Récup PV initiaux
        int pvInitial = fightService.getCombat(user1).getPv(user2);

        // Lancer attaque spéciale
        fightService.HandleAttach(user1, "special");

        // Vérif que dégâts ont été appliqués
        StateCombat combat = fightService.getCombat(user2);
        int pvAfterSpecial = combat.getPv(user2);

        assertTrue(pvAfterSpecial < pvInitial, "L'attaque spéciale doit infliger des dégâts");
        assertEquals(user2, combat.getPlayerNow(), "C'est maintenant à l'adversaire de jouer.");
    }

    @Test
    @Order(5)
    @DisplayName("Test attaque spéciale refusée si compteur insuffisant")
    public void TestSpecialAttackFailsIfNotReady() {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        // Forcer un compteur insuffisant
        p1.CompteurAttack(0); //0 donc trop tot

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // PV de référence avant attaque spéciale refusée
        int pvAvant = fightService.getCombat(user1).getPv(user2);

        // Essayer attaque spéciale, rien faire en toute logique
        fightService.HandleAttach(user1, "special");

        // PV après tentative
        int pvApres = fightService.getCombat(user1).getPv(user2);

        // pas de prte de pv attack spe pas diospo
        assertEquals(pvAvant, pvApres, "L'attaque spéciale ne doit pas infliger de dégâts si le compteur est insuffisant");

        //tour doit pas etre incrementé
        assertEquals(user1, fightService.getCombat(user1).getPlayerNow(), "Le joueur conserve son tour après une attaque spéciale refusée");
    }

    @Test
    @Order(6)
    @DisplayName("Test victoire par KO (PV à 0)")
    public void testVictoireParKO() throws Exception {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        // Attack spe pour kill vite l adversaire
        p1.CompteurAttack(p1.getRestrictionAttackSpecial());

        // pv a 10 pour kill vite
        p2.setPV(5);

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // lancer attack qui devrait vite kill l adversaire
        fightService.HandleAttach(user1, "special");

        //Premier appel combat encore visible
        StateCombat visible = fightService.getCombat(user2);
        assertNotNull(visible, "Le combat doit encore être visible une fois");

        // 2 appel : combat sup logiquement
        StateCombat shouldBeNull = fightService.getCombat(user2);
        assertNull(shouldBeNull, "Le combat doit être supprimé après affichage final");

        // verif bien que le dernier gagnant est enregistré
        String gagnant = fightService.getLastWinner(user1);
        assertEquals(user1, gagnant, "user1 doit être le gagnant enregistré");
    }

    @Test
    @Order(7)
    @DisplayName("TTest échec si objet inexistant utilisé")
    public void testUseInvalidObjectFailsGracefully() throws Exception {

        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        int pvAvant = fightService.getCombat(user1).getPv(user2);


        // use un ID d’objet inexistant
        fightService.useObject(user1, "objetQuiExistePas");

        // Les PV ne doivent pas avoir changé car pas object dispo
        int pvApres = fightService.getCombat(user1).getPv(user2);
        assertEquals(pvAvant, pvApres, "L'utilisation d'un objet inexistant ne doit rien faire");

        // Le tour ne doit pas avoir changé non plus car pas eu d'attaqUE
        assertEquals(user1, fightService.getCombat(user1).getPlayerNow(), "Le tour ne doit pas changer après une erreur d’objet");
    }

    @Test
    @Order(8)
    @DisplayName("Test victoire par KO avec objet du CoffreDesJoyaux")
    public void testKOAvecObjetDuCoffre() throws Exception {


        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");


        // pv a 5 pour kill vite
        p2.setPV(5);

        // Création de l’arme dans le coffre
        Weapon weapon = new Weapon("Epee en bois",23,1,2,5);
        weapon.setId("weapon");

        CoffreDesJoyaux coffre = new CoffreDesJoyaux();
        coffre.setId("coffre1");
        coffre.setContenu(List.of(weapon));


        // Le coffre est mis dans le backpack
        List<ObjectBase> bp1 = List.of(coffre);
        List<ObjectBase> bp2 = List.of();

        // Création du combat
        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // Utilisation de l'objet contenu dans le coffre
        fightService.useObject(user1, "weapon");


        // 1 appel  supprimé
        StateCombat supprimé = fightService.getCombat(user2);
        assertNull(supprimé, "Le combat doit être supprimé après affichage");

        // verif que le gagnant est bien enregistré
        String gagnant = fightService.getLastWinner(user1);
        assertEquals(user1, gagnant, "user1 doit être enregistré comme gagnant");

    }
    }





