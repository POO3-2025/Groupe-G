package be.helha.labos.crystalclash.DTO;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.Instant;
import java.util.List;

public class FightHistory {
    private String winnerName;
    private String loserName;
    private String  timestamp;

    public FightHistory() {
        // Constructeur vide requis pour la désé
    }

    public FightHistory(String winnerName, String loserName ) {
        this.winnerName = winnerName;
        this.loserName = loserName;
        this.timestamp = Instant.now().toString(); //Instant t du combat
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonGetter("timestamp")
    public String getTimestampAsString() {
        return timestamp != null ? timestamp.toString() : null;
    }
}
