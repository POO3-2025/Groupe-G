package be.helha.labos.crystalclash.DTO;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Object.ObjectBase;
import java.util.*;
public class StateCombat {

    private String Player1;
    private String Player2;
    private Personnage Character1;
    private Personnage Character2;
    private int Pv1;
    private int Pv2;
    private String PlayerNow;
    private Map<String, List<ObjectBase>> backpack = new HashMap<>();
    private List<String> logCombat = new ArrayList<>();
    private int tour = 1;

    public StateCombat(String Player1, String Player2, Personnage Character1, Personnage Character2,
                       List<ObjectBase> bp1, List<ObjectBase> bp2) {
        this.Player1 = Player1;
        this.Player2 = Player2;
        this.Character1 = Character1;
        this.Character2 = Character2;
        this.Pv1 = Character1.getPV();
        this.Pv2 = Character2.getPV();
        this.PlayerNow = Player1;
        backpack.put(Player1, bp1);
        backpack.put(Player2, bp2);
    }

    //Cool pour un combat player1 va return player 2 et inversement
    public String getOpponent(String Player) {
        return Player.equals(Player1) ? Player2 : Player1;
    }

    public Personnage getCharacter(String Player) {
        return Player.equals(Player1) ? Character1 : Character2;
    }

    public int getPv(String player) {
        if (player == null) return 0;
        return player.equals(Player1) ? Pv1 : Pv2;
    }

    public void setPv(String player, int pv) {
        if (player == null) return;
        if (player.equals(Player1)) this.Pv1 = pv;
        else if (player.equals(Player2)) this.Pv2 = pv;
    }

    public List<ObjectBase> getBackpack(String Player){
        return backpack.getOrDefault(Player, new ArrayList<>());
    }

    public String getPlayerNow(){

        return PlayerNow;
    }

    public void NextTurn(){
        this.PlayerNow = getOpponent(PlayerNow);
        this.tour++;
    }
    public int getTour() {
        return tour;
    }


    public List<String> getLog() {
        return logCombat;
    }

    public void addLog(String action) {
        logCombat.add("Tour " + tour + " - " + action);
    }

    public boolean isFinished() {
        return Pv1 <= 0 || Pv2 <= 0;
    }

    public String getWinner() {
        if (Pv1 <= 0) return Player2;
        if (Pv2 <= 0) return Player1;
        return null;
    }
}
