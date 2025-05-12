package be.helha.labos.crystalclash.Service;


import be.helha.labos.crystalclash.DAO.FightDAO;
import be.helha.labos.crystalclash.DAO.ShopDAO;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class FightService {

    private final FightDAO fightDAO; //classement
    @Autowired
    private CharacterService characterService;//Save backPack perso
    @Autowired
    private UserService userService;//Donner récompense aux gagnant, win/lose

    /**
     * Pailre clés valeurs (String id ou nom et StateCombat l'etat du combat), associe un nom de user a 1 statecombat partagé entre 2 users
     * */
    private final Map<String, StateCombat> combats = new HashMap<>();

    /**
     * Va stocker le gagnant. apres que comabt soit retiré de comabts
     * **/
    private final Map<String, String> derniersGagnants = new HashMap<>();
    @Autowired
    private InventoryService inventoryService;

    public FightService(FightDAO fightDAO) {
        this.fightDAO = fightDAO;
    }

    /**
     * Cration du comabt
     * @param p1 = user1
     * @param p2
     * @param char1 = perso du user 1
     * @param char2
     * @param bp1 = contennu du backPack1 du perso1 qui appartient au user1
     * @param bp2
     * Crée un new objet StateCombat avec les parametres et ce même objet sera stocké sous p1 et p2 dans combats
     * **/
    public void createCombat(String p1, String p2, Personnage char1, Personnage char2,
                             List<ObjectBase> bp1, List<ObjectBase> bp2) {
        System.out.println("[DEBUG] FightService - createCombat() entre " + p1 + " et " + p2);

        int bonusPv1 = getArmure(p1);
        int bonusPv2 = getArmure(p2);

        char1.setPV(char1.getPV() + bonusPv1);
        char2.setPV(char2.getPV() + bonusPv2);



        StateCombat state = new StateCombat(p1, p2, char1, char2, bp1, bp2);
        System.out.println("[DEBUG] Backpack joueur1 (" + p1 + ") : " + bp1);
        System.out.println("[DEBUG] Backpack joueur2 (" + p2 + ") : " + bp2);
        System.out.println("[DEBUG] Personnage joueur1 (" + p1 + ") : " + char1);
        System.out.println("[DEBUG] Personnage joueur2 (" + p2 + ") : " + char2);

        CoffreDesJoyaux coffre1 = inventoryService.getCoffreDesJoyauxForUser(p1);
        CoffreDesJoyaux coffre2 = inventoryService.getCoffreDesJoyauxForUser(p2);

        // Vérifie qu’ils ne sont pas nuls
        if (coffre1 != null) state.setcoffreDreJoyaux(p1, coffre1.getContenu());
        if (coffre2 != null) state.setcoffreDreJoyaux(p2, coffre2.getContenu());

        combats.put(p1, state);
        combats.put(p2, state);
    }



    /**
     * @param username
     * Recupérer un comabt
     * Si le comabt est en cours on retourne StateCombat, si est trminé on fait un StateCombat juste avec le gagnant( PV = 1 gagnant, perdu 0)
     * renvoie un état vide avec juste le nom du vainqueur, suffisant pour l'affichage final
     * **/
    public StateCombat getCombat(String username) {
        StateCombat state = combats.get(username);
        if (state != null) {
            if (state.isFinished()) {

                resolveWinnerAndLoser(state);

                if (!state.isCombatDisplayed()) {
                    state.setCombatDisplayed(true);
                    return state;
                }

                // Combat déjà affiché une fois , alors suppression
                String winner = state.getWinner();
                String loser = state.getLoser();
                if (winner != null && loser != null) {
                    derniersGagnants.put(winner, winner);
                    derniersGagnants.put(loser, winner);
                    combats.remove(winner);
                    combats.remove(loser);
                }
                return null;
            }

            return state;
        }

        return null;
    }




    //Get pour avoir le dernier gagnant
    public String getLastWinner(String username) {
        return derniersGagnants.get(username);
    }


    /**
     * Attaque normal et spécial
     * Recupé l'état du comabt
     * si attaque normal, on inflige dégat  de base et incrémente le compteur d'attack
     * si special, regarde si le joueur a assez de vie, si oui ok inflige dégats et remet le compteur a zéro car l'attaque spé n'est que dispo a partir d'un certain nbr de tour
     * et a jour pv du user
     * Comabt terminée on recup le winner et loserr, on donne les récompense, stats gagnant, perdant
     * met a jour le dernier gagnant et on delete le combat
     * */
    public void HandleAttach(String Player, String type) {
        System.out.println("[DEBUG] useObject() appelé par " + Player + " avec  " + type);

        if (Player == null || type == null) return; // sécurité

        StateCombat state = combats.get(Player);
        if (state == null || Player == null || !Player.equals(state.getPlayerNow())) {
            return;
        }

        Personnage attack = state.getCharacter(Player);
        String oppenent = state.getOpponent(Player);
        int Damage;

        if (type.equals("normal")) {
            Damage = attack.getAttackBase();
            attack.CompteurAttack(attack.getCompteurAttack() + 1);
            state.addLog(Player + " utilise " + attack.getNameAttackBase() + " (" + Damage + " damage)");
        }else{
            if(attack.getCompteurAttack() < attack.getRestrictionAttackSpecial()){
                state.addLog(Player + " ne peut pas utiliser son attaque spéciale !");
                return;
            }
            Damage = attack.getAttackSpecial();
            attack.CompteurAttack(0); //remet compteur a zero
            state.addLog(Player + " utilise " + attack.getNameAttaqueSpecial() + " (" + Damage + " damage)");
        }

        int newPv = state.getPv(oppenent) - Damage;
        state.setPv(oppenent, newPv);
        if (state.isFinished()) {
            String winner = state.getWinner();
            String loser = state.getLoser();


            try {
                userService.rewardWinner(winner, 50, 1);
                userService.IncrementWinner(winner);
                userService.incrementDefeat(loser);
            } catch (Exception e) {
                System.out.println("[ERREUR] Mise à jour des statistiques : " + e.getMessage());
                e.printStackTrace(); // Facultatif mais utile
            }

            state.addLog(winner + " remporte le combat ! +1 niveau, +50 cristaux");
            derniersGagnants.put(winner, winner);
            derniersGagnants.put(loser, winner);
            combats.remove(winner);
            combats.remove(loser);
            state.setWinner(winner);
            state.setLoser(loser);
            return;
        }
        state.NextTurn();

    }


    public void useObject(String Player, String objectId) throws Exception {
        System.out.println("[DEBUG] useObject() appelé par " + Player + " avec objet " + objectId);

        if (Player == null || objectId == null) return; // sécurité

        StateCombat state = combats.get(Player);
        if (state == null || !Player.equals(state.getPlayerNow())) {
            return;
        }

        List<ObjectBase> backpack = state.getBackpack(Player);
        List<ObjectBase> coffre = state.getcoffreDreJoyaux(Player);//recup coffre
        Optional<ObjectBase> objet = backpack.stream().filter(o -> o.getId().equals(objectId)).findFirst();
        if(objet.isEmpty())return;

        boolean fromCoffre = false;
        if(objet.isEmpty()){
            objet = coffre.stream().filter(o -> o.getId().equals(objectId)).findFirst();
            fromCoffre = true;
        }

        ObjectBase obj = objet.get();
        Personnage perso = state.getCharacter(Player);

        if(obj instanceof Weapon){
            int dmg = ((Weapon) obj).getDamage();
        String oppenent = state.getOpponent(Player);
        int NewPv = state.getPv(oppenent) - dmg;
        state.setPv(oppenent, NewPv);
        state.addLog(Player + " utilise " + obj.getName() + " et inflige " + dmg + " dégâts !");
        }else
        if(obj instanceof HealingPotion){
            int pv = state.getPv(Player);
            int heal = ((HealingPotion) obj).getHeal();
            state.setPv(Player, heal);
            state.addLog(Player + " boit une potion de soin (+ " + heal + " PV)");
        }/*else
        if(obj instanceof Armor){
            int bonus = ((Armor) obj).getBonusPV();
            int pv = state.getPv(Player);
            state.setPv(Player, pv + bonus);
            state.addLog(Player + " utilise " + obj.getName() + " et gagne " + bonus + " PV temporairement");
        }else if(obj instanceof PotionOfStrenght){
            int bonus = ((PotionOfStrenght) obj).getBonusATK();
            state.addLog(Player + " boit une " + obj.getName() + " et gagne +" + bonus + " en attaque !");
        }*/
        obj.Reducereliability();
        if (!obj.IsUsed()) {
            backpack.remove(obj); // Supprime si fiabilité 0
        }
        if (state.isFinished()) {
            resolveWinnerAndLoser(state);
            String winner = state.getWinner();
            String loser = state.getLoser();

            try {
                userService.rewardWinner(winner, 50, 1);
                userService.IncrementWinner(winner);
                userService.incrementDefeat(loser);
            } catch (Exception e) {
                System.out.println("[ERREUR] Mise à jour des statistiques : " + e.getMessage());
                e.printStackTrace(); // Facultatif mais utile
            }

            state.addLog(winner + " remporte le combat ! +1 niveau, +50 cristaux");
            derniersGagnants.put(winner, winner);
            derniersGagnants.put(loser, winner);
            //trophé
            userService.incrementWimConsecutive(winner);
            userService.resetWinConsecutives(loser);
            combats.remove(winner);
            combats.remove(loser);
            state.setWinner(winner);
            state.setLoser(loser);

        }else {
            state.NextTurn();
        }

        //Test de mettre a jour le backPack pour l endurance des armes
        //Va sauvegarder le back modfié avec les nouvelles valeurs d'endurerances
        try{
            BackPack backPack = new BackPack();
            backPack.setObjets(backpack);
            characterService.saveBackPackForCharacter(Player, backPack);
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour du backpack : " + e.getMessage());
        }

    }

    public void forfait(String username) throws Exception {
        StateCombat state = combats.get(username);
        if (state == null) return;

        String opponent = state.getOpponent(username);
        if (opponent == null) return;

        // met  PV du joueur qui abandonne à 0 pour terminer le combat
        state.setPv(username, 0);
        resolveWinnerAndLoser(state);
        if (state.isFinished()) {

            //Recup win/loser via resolveWinnerAndLoser
            String winner = state.getWinner();
            String loser = state.getLoser();

            userService.rewardWinner(winner, 50, 1);
            userService.IncrementWinner(winner); // +1 victoire pour le gagnant
            userService.incrementDefeat(loser);
            state.addLog(username + " a abandonné le combat.");
            state.addLog(winner + " remporte le combat par forfait ! +1 niveau, +50 cristaux");

            //enregistre le dernier gagnant associé aux 2 joueurs
            derniersGagnants.put(winner, winner);
            derniersGagnants.put(loser, winner);

            userService.incrementWimConsecutive(winner);
            userService.resetWinConsecutives(loser);

            //retire combat de la memoire (MAP), on libere juste de la memoire
            //Inconiant car direct a la fin du combat c delete
            combats.remove(username);
            combats.remove(opponent);
        }
    }


    public List<UserInfo> getClassementPlayer() {
    return fightDAO.getClassementPlayer();
    }

    //Methodes pour centraliser, le gagant et le loser si pas deja fait
    //et ducoup mofid l objet StateComabt
    private void resolveWinnerAndLoser(StateCombat state) {

        if(state == null || !state.isFinished()) return;

        // Si winner/loser  déjà définis rien a  faire
        if (state.getWinner() != null && state.getLoser() != null) return;


        String winner = (state.getPv(state.getPlayer1()) > 0) ? state.getPlayer1() : state.getPlayer2();
        String loser = state.getOpponent(winner);
        state.setWinner(winner);
        state.setLoser(loser);
    }


    //gere l'armure
    public int getArmure(String username){
        Equipment equipment = characterService.getEquipmentForCharacter(username);
        if(equipment == null){
            return 0;
        }
        return equipment.getObjets().stream()
            .filter(obj -> obj instanceof Armor)
            .mapToInt(obj -> ((Armor) obj).getBonusPV())
            .sum();
    }

    }
