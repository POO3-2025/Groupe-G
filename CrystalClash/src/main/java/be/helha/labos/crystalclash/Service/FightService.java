package be.helha.labos.crystalclash.Service;


import be.helha.labos.crystalclash.DAO.FightDAO;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Object.*;
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
    @Autowired
    private InventoryService inventoryService;
    /**
     * Pailre clés valeurs (String id ou nom et StateCombat l'etat du combat), associe un nom de user a 1 statecombat partagé entre 2 users
     * */
    private final Map<String, StateCombat> combats = new HashMap<>();

    /**
     * Va stocker le dernier gagnant. apres que comabt soit retiré de comabts même apres supression du combat en mémoire
     * **/
    private final Map<String, String> derniersGagnants = new HashMap<>();


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

        //Ajoute combat aux deux joueurs dans combats
        combats.put(p1, state);
        combats.put(p2, state);
    }



    /**
     * @param username
     * Recupérer un comabt
     * Si le comabt est en cours on retourne StateCombat, si est terminé on fait un StateCombat juste avec le gagnant( PV = 1 gagnant, perdu 0)
     * renvoie un état vide avec juste le nom du vainqueur, suffisant pour l'affichage final
     * **/
    public StateCombat getCombat(String username) {
        StateCombat state = combats.get(username);

        if (state == null) return null;

        // le combat est terminé
        if (state.isFinished()) {
            if (!state.isCombatDisplayed()) {
                resolveWinnerAndLoser(state); // obligatoirement ici

                state.setCombatDisplayed(true);
                state.setReadyToBeCleaned(true); // marquer pour suppression à la prochaine lecture

                return state; // ← ON AUTORISE une dernière visualisation
            }
            int endurance = getArmoRelibility(username);
            state.setArmorReliability(username, endurance);

            // combat a déjà été affiché → on supprime maintenant
            combats.remove(state.getPlayer1());
            combats.remove(state.getPlayer2());
            return null;
        }

        // combat en cours
        int endurance = getArmoRelibility(username);
        state.setArmorReliability(username, endurance);

        return state;
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
            Integer bonusATK = state.getBonusATKTemporaire().remove(Player); //retire le bonus a l'attaque (remove que 1 usage)
            if (bonusATK != null) {
                Damage += bonusATK;
            }
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

        //oppenent adversaire
        userArmorReliability(oppenent, state);

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
            state.setReadyToBeCleaned(true);
            return;
        }
        state.NextTurn();

    }

    /**
     * utiliser  un objet
     * @param Player
     * @param objectId
     * @throws Exception
     * */
    public void useObject(String Player, String objectId) throws Exception {
        System.out.println("[DEBUG] useObject() appelé par " + Player + " avec objet " + objectId);

        if (Player == null || objectId == null) return; // sécurité

        StateCombat state = combats.get(Player);
        if (state == null || !Player.equals(state.getPlayerNow())) {
            return;
        }

        List<ObjectBase> backpack = state.getBackpack(Player);
        List<ObjectBase> coffre = state.getChest(Player);
        Optional<ObjectBase> objet = backpack.stream().filter(o -> o.getId().equals(objectId)).findFirst();
        boolean fromcoffredesjoyaux = false;

        if (objet.isEmpty()) {
            objet = coffre.stream().filter(o -> o.getId().equals(objectId)).findFirst();
            fromcoffredesjoyaux = true;
        }

        if (objet.isEmpty()) return;

        ObjectBase obj = objet.get();
        Personnage perso = state.getCharacter(Player);

        if(obj instanceof Weapon){
            int dmg = ((Weapon) obj).getDamage();
            String oppenent = state.getOpponent(Player);
            int NewPv = state.getPv(oppenent) - dmg;
            state.setPv(oppenent, NewPv);
            state.addLog(Player + " utilise " + obj.getName() + " et inflige " + dmg + " dégâts !");
            userArmorReliability(oppenent, state);
        }else
        if(obj instanceof HealingPotion){
            int pv = state.getPv(Player);
            int heal = ((HealingPotion) obj).getHeal();
            state.setPv(Player, pv + heal);
            state.addLog(Player + " boit une potion de soin (+ " + heal + " PV)");

        }else if(obj instanceof PotionOfStrenght){
            int bonus = ((PotionOfStrenght) obj).getBonusATK();
            state.getBonusATKTemporaire().put(Player,bonus);//Ajoute effet de force a l attaque normale du perso
            state.addLog(Player + " boit une " + obj.getName() + " et gagne +" + bonus + " en attaque !");
        }

        obj.Reducereliability();

        if (!obj.IsUsed()) {
            if (fromcoffredesjoyaux) {
                coffre.remove(obj);
            } else {
                backpack.remove(obj);
            }
        }

        //Test de mettre a jour le backPack pour l endurance des armes
        //Va sauvegarder le back modfié avec les nouvelles valeurs d'endurerances
        try{
            BackPack backPack = new BackPack();
            backPack.setObjets(backpack);
            for(ObjectBase ob : backpack){
                if(ob instanceof  CoffreDesJoyaux chest){
                    chest.setContenu(coffre);
                }
            }
            characterService.saveBackPackForCharacter(Player, backPack);
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour du backpack : " + e.getMessage());
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
            state.setReadyToBeCleaned(true);

        }else {
            state.NextTurn();
        }




    }

    /**
     * Abandonne le combat
     * @param username
     * @throws Exception
     */
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

            //supp pas direct permet affichage avec le winner et loser
            state.setCombatDisplayed(false);
        }
    }

    /**
     * Recupere le classement des joueurs
     * @return
     */
    public List<UserInfo> getClassementPlayer() {
        return fightDAO.getClassementPlayer();
    }

    /**
     * @param state
     * Methodes pour centraliser, le gagant et le loser si pas deja fait
     *  et ducoup mofid l objet StateComabt
     * **/

    private void resolveWinnerAndLoser(StateCombat state) {

        //Si objet null j'fais rien
        if(state == null || !state.isFinished()) return;

        // Si winner/loser  déjà définis rien a  faire
        if (state.getWinner() != null && state.getLoser() != null) return;

        //Si player1 a encore des PV > 0 alors il est le gagnant sinon player2
        String winner = (state.getPv(state.getPlayer1()) > 0) ? state.getPlayer1() : state.getPlayer2();
        String loser = state.getOpponent(winner);
        state.setWinner(winner); //enregistre le winner dans le setWInner
        state.setLoser(loser);
    }


    //gere l'armure

    /**
     * recupere un armure
     * @param username
     * @return
     */
    public int getArmure(String username){
        Equipment equipment = characterService.getEquipmentForCharacter(username);
        if(equipment == null){
            return 0;
        }
        return equipment.getObjets().stream() // transforme liste ObjectBase dans l'equipement en un flux stream
            .filter(obj -> obj instanceof Armor)//Filtre pour garder que ceux qui sont une instance de Armor
            .mapToInt(obj -> ((Armor) obj).getBonusPV())//cast objet restant en Armor puis appelle getBonusPV
            .sum();//Ajoute le bonus de PV a ses points de vies.
    }



    /**
     * @param state = servir a ajoute un log voulu
     * @param username
     * Gestion de l'endurence de l'armure pdt le combat
     * update voir si il a recu des dégats et si il possede au moins une armure
     * **/
    public void userArmorReliability(String username, StateCombat state) {

        try{
            Equipment equip = characterService.getEquipmentForCharacter(username);//Appelle méthode service
            boolean update = false; //Savoir si il y a eu une update

            for(ObjectBase ob : equip.getObjets()){
                if(ob instanceof Armor armor){

                    armor.Reducereliability();
                    if(armor.getReliability() <= 0){
                        state.addLog(username + "a perdu sur son armure" + armor.getName() + "(cassée");
                    }else{
                        state.addLog(username + "perd 1 de fiabilité sur son armure" + armor.getName());
                    }
                    update = true;
                }
            }
            if (update){
                //SI update a lieu alors on appele une methode du service
                characterService.saveEquipmentForCharacter(username, equip);
            }
        } catch (Exception e) {
            System.out.println("[ERREUR] Mise à jour fiabilité armure de " + username + " : " + e.getMessage());
        }

    }




    /**
     * @param username*Avoir equipement endurence coté client
     *                       encore ici si -1 alors pas d'armure
     *                       for = parcourt tous les objets  de l'equip si un ets = a l'instance Armor alors retourne son endurence
     **/
    public int getArmoRelibility(String username) {

        Equipment equipment = characterService.getEquipmentForCharacter(username);
        if (equipment == null) return -1;
        for (ObjectBase ob : equipment.getObjets()) {
            if (ob instanceof Armor armor) { //Si correspond bien a une armure alors on retourne son endu
                return armor.getReliability();
            }

        } return -1; //pas d'armure
    }



       /*
    //Set pour les tets pour que ce soit accessible pour les tests
    //public pour y avoir acces, void retourne rien et ensuite un set et a l'interieure un  this..... : fait réference a l'attribut privée de la classe voulue
    */
    /***
     * @param characterService
     * */
    public void setCharacterService(CharacterService characterService) {
        this.characterService = characterService;
    }

    /**
     * @param userService
     * */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /***
     * @param inventoryService
     * */
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public CharacterService getCharacterService() {
        return this.characterService;
    }

}
