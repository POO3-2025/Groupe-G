package be.helha.labos.crystalclash.DTO;

import java.time.Instant;
import java.util.List;

public class FightHistory {
    private String winner;
    private String loser;
    private Instant timestamp;
    private List<String> combatLog;

    public FightHistory() {
        // Constructeur vide requis pour la désé
    }

    public FightHistory(String winner, String loser, List<String> combatLog) {
        this.winner = winner;
        this.loser = loser;
        this.combatLog = combatLog;
        this.timestamp = Instant.now(); //Instant t du combat
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getCombatLog() {
        return combatLog;
    }

    public void setCombatLog(List<String> combatLog) {
        this.combatLog = combatLog;
    }
}
