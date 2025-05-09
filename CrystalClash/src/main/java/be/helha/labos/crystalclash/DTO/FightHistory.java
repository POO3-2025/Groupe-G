package be.helha.labos.crystalclash.DTO;

import java.time.Instant;
import java.util.List;

public class FightHistory {
    private String winnerName;
    private String loserName;
    private Instant timestamp;
    private List<String> combatLog;

    public FightHistory() {
        // Constructeur vide requis pour la désé
    }

    public FightHistory(String winnerName, String loserName, List<String> combatLog) {
        this.winnerName = winnerName;
        this.loserName = loserName;
        this.combatLog = combatLog;
        this.timestamp = Instant.now(); //Instant t du combat
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getLoserName() {
        return loserName;
    }

    public void setLoserName(String loserName) {
        this.loserName = loserName;
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
