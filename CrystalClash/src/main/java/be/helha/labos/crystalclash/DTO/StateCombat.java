package be.helha.labos.crystalclash.DTO;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Object.ObjectBase;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 *Son role est d avoir toutes les infos pour représenter le comabt en cours
 * Les 2 jouers
 * PV
 * Objets
 * Log du combat
 * le joueur actuelle
 * num du tour
 **/
public class StateCombat {

    private String player1;
    private String player2;
    @JsonProperty("character1") //nomme le chmap ds le JSON  transmis (Jackson utile)
    private Personnage character1;
    @JsonProperty("character2")
    private Personnage character2;
    @JsonProperty("pv1")
    private int pv1;
    @JsonProperty("pv2")
    private int pv2;
    private String playerNow;
    @JsonProperty("backpack")
    private Map<String, List<ObjectBase>> backpack = new HashMap<>();
    @JsonProperty("logCombat")
    private List<String> logCombat = new ArrayList<>(); //Contient user joue, tour et historique des actions
    private int tour = 1;
    @JsonProperty("winner")
    private String winner;
    @JsonProperty("loser")
    private String loser;

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
    }

    //Cool pour un combat player1 va return player 2 et inversement, retourne liste des 2 users
    public String getOpponent(String Player) {
        return Player.equals(player1) ? player2 : player1;
    }

    public Personnage getCharacter(String Player) {
        return Player.equals(player1) ? character1 : character2;
    }

    public int getPv(String player) {
        System.out.println(">>> getPv appelé avec : " + player);
        System.out.println(">>> player1 = " + player1 + " / pv1 = " + pv1);
        System.out.println(">>> player2 = " + player2 + " / pv2 = " + pv2);
        if (player == null) return 0;
        return player.equals(player1) ? pv1 : pv2;
    }

    public void setPv(String player, int pv) {
        if (player == null) return;
        if (player.equals(player1)) this.pv1 = pv;
        else if (player.equals(player2)) this.pv2 = pv;
    }

    public void setBackpack(Map<String, List<ObjectBase>> backpack) {
        this.backpack = (backpack != null) ? backpack : new HashMap<>();
    }
//Recup contenu du back
    public List<ObjectBase> getBackpack(String username) {
        if (backpack == null) return new ArrayList<>();
        return backpack.getOrDefault(username, new ArrayList<>());
    }

    public String getPlayerNow(){

        return playerNow;
    }

    public void NextTurn(){
        this.playerNow = getOpponent(playerNow);
        this.tour++;
    }
    public int getTour() {
        return tour;
    }


    public List<String> getLog() {
        if (logCombat == null) logCombat = new ArrayList<>();
        return logCombat;
    }

    public void addLog(String action) {
        logCombat.add("Tour " + tour + " - " + action);
    }

    public boolean isFinished() {
        return pv1 <= 0 || pv2 <= 0;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser;
    }


    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    private boolean combatDisplayed = false;

    public boolean isCombatDisplayed() {
        return combatDisplayed;
    }

    public void setCombatDisplayed(boolean combatDisplayed) {
        this.combatDisplayed = combatDisplayed;
    }
}
