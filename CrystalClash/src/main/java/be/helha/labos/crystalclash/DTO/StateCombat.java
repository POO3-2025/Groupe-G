package be.helha.labos.crystalclash.DTO;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Object.ObjectBase;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
public class StateCombat {

    private String player1;
    private String player2;
    private Personnage character1;
    private Personnage character2;
    private int pv1;
    private int pv2;
    private String playerNow;
    private Map<String, List<ObjectBase>> backpack = new HashMap<>();
    @JsonProperty("logCombat")
    private List<String> logCombat = new ArrayList<>();
    private int tour = 1;

    public StateCombat(String player1, String player2, Personnage character1, Personnage character2,
                       List<ObjectBase> bp1, List<ObjectBase> bp2) {
        this.player1 = player1;
        this.player2 = player2;
        this.character1 = character1;
        this.character2 = character2;
        this.pv1 = character1.getPV();
        this.pv2 = character2.getPV();
        this.playerNow =player1;
        backpack.put(player1, bp1);
        backpack.put(player2, bp2);
    }

    //Cool pour un combat player1 va return player 2 et inversement
    public String getOpponent(String Player) {
        return Player.equals(player1) ? player2 : player1;
    }

    public Personnage getCharacter(String Player) {
        return Player.equals(player1) ? character1 : character2;
    }

    public int getPv(String player) {
        if (player == null) return 0;
        return player.equals(player1) ? pv1 : pv2;
    }

    public void setPv(String player, int pv) {
        if (player == null) return;
        if (player.equals(player1)) this.pv1 = pv;
        else if (player.equals(player2)) this.pv2 = pv;
    }

    public void setBackpack(Map<String, List<ObjectBase>> backpack) {
        this.backpack = backpack;
    }

    public List<ObjectBase> getBackpack(String username) {
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
        if (pv1 <= 0) return player2;
        if (pv2 <= 0) return player1;
        return null;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }
}