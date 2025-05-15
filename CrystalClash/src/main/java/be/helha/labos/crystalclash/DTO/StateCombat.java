package be.helha.labos.crystalclash.DTO;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.CharacterService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Classe représentant l'état d'un combat en cours.
 * Cette classe contient toutes les informations nécessaires pour représenter un combat :
 * - Les deux joueurs
 * - Les points de vie (PV) des personnages
 * - Les objets utilisés pendant le combat
 * - Le journal des actions du combat
 * - Le joueur actuellement actif
 * - Le numéro du tour
 */
public class StateCombat {


    private String player1; //Nom du joueur1
    private String player2;//Nom du joueur2
    @JsonProperty("character1") //nomme le champ ds le JSON  transmis (Jackson utile)
    private Personnage character1; //perso du user, objet issu de la calsse Personnage
    @JsonProperty("character2")
    private Personnage character2;
    @JsonProperty("pv1")
    private int pv1; //Pv du perso (modifié au fil du combat)
    @JsonProperty("pv2")
    private int pv2;
    private String playerNow; //Celui qui doit jouer le tour actuel (change avec NextTurn)
    @JsonProperty("backpack")
    private Map<String, List<ObjectBase>> backpack = new HashMap<>(); //Liste d'objet use pdt le combat (cle = username, valeur ObjectBase)
    @JsonProperty
    private Map<String, List<ObjectBase>> chest = new HashMap<>(); //Contient objets tirés depuis CoffreDesjoayux
    @JsonProperty("logCombat")
    private List<String> logCombat = new ArrayList<>(); //Contient user joue, tour et historique des actions
    private int tour = 1;
    @JsonProperty("winner")
    private String winner; //Winner fin de combat
    @JsonProperty("loser")
    private String loser; //Loser fin de combat
    /**
     * Map pour mémoriser temporairement que le user a un bonus temporaire de force a appliqué au prochain tour
     * */
    private final Map<String, Integer> BonusAtkTemp = new HashMap<>();
    //Cle-Valeur , ici c le username et l'endurence de l'armure
    @JsonProperty("armorReliabilities") //force JackSon
    private Map<String, Integer> armorReliabilities = new HashMap<>();

    private boolean readyToBeCleaned = false; //Pour effacer apres combat


    /**
     * Constructeur de la classe StateCombat.
     * Initialise les champs, remplit les PV initiaux depuis les personnages,
     * définit le joueur 1 comme actif et extrait le contenu des coffres si présent dans le backpack.
     *
     * @param player1 Nom du joueur 1
     * @param player2 Nom du joueur 2
     * @param character1 Personnage du joueur 1
     * @param character2 Personnage du joueur 2
     * @param bp1 Backpack du joueur 1
     * @param bp2 Backpack du joueur 2
     */
    public StateCombat(String player1, String player2, Personnage character1, Personnage character2,
                       List<ObjectBase> bp1, List<ObjectBase> bp2) {
        this.player1 = player1;
        this.player2 = player2;
        this.character1 = character1;
        this.character2 = character2;
        this.pv1 = character1.getPV();
        this.pv2 = character2.getPV();

        System.out.println(">>> Création du combat");
        System.out.println(">>> " + player1 + " avec personnage " + character1.getClass().getSimpleName() + " - PV : " + pv1);
        System.out.println(">>> " + player2 + " avec personnage " + character2.getClass().getSimpleName() + " - PV : " + pv2);

        this.playerNow =player1;
        this.backpack.put(player1, bp1 != null ? bp1 : new ArrayList<>());
        this.backpack.put(player2, bp2 != null ? bp2 : new ArrayList<>());
        this.chest.put(player1, exrtactContentChest(bp1));
        this.chest.put(player2, exrtactContentChest(bp2));
    }

    /**
     * Extrait le contenu des coffres présents dans le backpack.
     * @param bp Liste des objets du backpack
     * @return Liste des objets contenus dans le coffre
     * cherche CoffreDesJoyaux ds le backPack et retourne sa liste d'objets interne
     * **/
    private List<ObjectBase> exrtactContentChest(List<ObjectBase> bp) {
        for (ObjectBase obj : bp){
            if (obj instanceof CoffreDesJoyaux chest && chest.getReliability() > 0){
                return chest.getContenu();
            }
        }
        return new ArrayList<>();
    }

    //Cool pour un combat player1 va return player 2 et inversement, retourne liste des 2 users

    /**
     * Retourne l'adversaire du joueur spécifié.
     * @param Player le nom du joueur actuel
     * @return le nom de son adversaire
     */
    public String getOpponent(String Player) {
        return Player.equals(player1) ? player2 : player1;
    }

    /**
     * Recupere le personnage du joueur spécifié.
     * @param Player
     * @return Le personnage du joueur
     */
    public Personnage getCharacter(String Player) {
        return Player.equals(player1) ? character1 : character2;
    }

    /**
     * Récupère les points de vie du joueur spécifié.
     * @param player
     * @return Les points de vie du joueur, ou 0 si le joueur est null.
     */
    public int getPv(String player) {
        System.out.println(">>> getPv appelé avec : " + player);
        System.out.println(">>> player1 = " + player1 + " / pv1 = " + pv1);
        System.out.println(">>> player2 = " + player2 + " / pv2 = " + pv2);
        if (player == null) return 0;
        return player.equals(player1) ? pv1 : pv2;
    }

    /**
     * change le PV du joueur
     * @param player
     * @param pv
     */
    public void setPv(String player, int pv) {
        if (player == null) return;
        if (player.equals(player1)) this.pv1 = pv;
        else if (player.equals(player2)) this.pv2 = pv;
    }

    /**
     *
     * @param backpack
     */
    public void setBackpack(Map<String, List<ObjectBase>> backpack) {
        this.backpack = (backpack != null) ? backpack : new HashMap<>();
    }
    //Recup contenu du back
    public List<ObjectBase> getBackpack(String username) {
        if (backpack == null) return new ArrayList<>();
        return backpack.getOrDefault(username, new ArrayList<>());
    }

    /**
     * Récupère le joueur actuellement actif.
     * @return Le nom du joueur actif.
     */
    public String getPlayerNow(){

        return playerNow;
    }

    /**
     * Change le joueur actif pour le prochain tour.
     */
    public void NextTurn(){
        this.playerNow = getOpponent(playerNow);
        this.tour++;
    }

    /**
     * Récupère le numéro du tour actuel.
     * @return Le numéro du tour.
     */
    public int getTour() {
        return tour;
    }

    /**
     *
     * @return
     */
    public List<String> getLog() {
        if (logCombat == null) logCombat = new ArrayList<>();
        return logCombat;
    }

    /**
     * Ajoute une action au journal de combat.
     * @param action
     */
    public void addLog(String action) {
        logCombat.add("Tour " + tour + " - " + action);
    }

    /**
     * Vérifie si le combat est terminé.
     * @return true si le combat est terminé, false sinon.
     */
    public boolean isFinished() {
        return pv1 <= 0 || pv2 <= 0;
    }

    /**
     * Récupère le gagnant du combat.
     * @return Le nom du gagnant.
     */
    public String getWinner() {
        return winner;
    }

    /**
     * Définit le gagnant du combat.
     * @param winner
     */
    public void setWinner(String winner) {
        this.winner = winner;
    }

    /**
     * Récupère le perdant du combat.
     * @return le perdant
     */
    public String getLoser() {
        return loser;
    }

    /**
     * Définit le perdant du combat.
     * @param loser
     */
    public void setLoser(String loser) {
        this.loser = loser;
    }

    /**
     * Récupère le nom du joueur 1.
     * @return
     */
    public String getPlayer1() {
        return player1;
    }

    /**
     * Récupère le nom du joueur 2.
     * @return
     */
    public String getPlayer2() {
        return player2;
    }

    /**
     *
     *
     */
    //Si a true alors la fenetre combat est deja ouverte
    private boolean combatDisplayed = false;

    /**
     * Vérifie si l'interface de combat est affichée.
     * @return true si l'interface est affichée, false sinon.
     */
    public boolean isCombatDisplayed() {
        return combatDisplayed;
    }

    /**
     * Définit si l’interface de combat est affichée.
     * @param combatDisplayed
     */
    public void setCombatDisplayed(boolean combatDisplayed) {
        this.combatDisplayed = combatDisplayed;
    }

    /**
     * change le contenu du coffre du joueur spécifié.
     * @param username
     * @param obj
     */
    //Coffre
    public void setchest(String username,List<ObjectBase> obj){
        this.chest.put(username, obj);
    }

    /**
     * Récupère le contenu du coffre du joueur spécifié.
     * @param username
     * @return
     */
    public List<ObjectBase> getChest(String username){
        return chest.getOrDefault(username, new ArrayList<>());
    }

    /**
     * Obtenir bonus atk temp
     * */
    public Map<String, Integer> getBonusATKTemporaire() {
        return BonusAtkTemp;
    }



    /**
    @param username
    @param reliability
    Changer la valuer de l'armure*Si map armorReliabilities pas encore initialisé alors on le fait
    et on y met le username ainsi que l endu de l'armure (clé-valuer)
    */
    public void setArmorReliability(String username, int reliability) {
        if (this.armorReliabilities == null) this.armorReliabilities = new HashMap<>();
        this.armorReliabilities.put(username, reliability);
    }

    /**
     * @param username
     * Obtenir l'armure
     * SI null alors on retourn -1, Map pas initaliser
     * le retun signifie que si defaultvalue vaut -1 alors pas d'armure
     * **/
    public int getArmorReliabilities(String username) {
        if (armorReliabilities == null) return -1;
        return armorReliabilities.getOrDefault(username, -1);
    }

    public boolean isReadyToBeCleaned() {
        return readyToBeCleaned;
    }

    /**
     *
     * @param readyToBeCleaned
     */
    public void setReadyToBeCleaned(boolean readyToBeCleaned) {
        this.readyToBeCleaned = readyToBeCleaned;
    }


}
