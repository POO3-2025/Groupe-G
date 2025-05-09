package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.DTO.FightHistory;
import java.util.List;
import java.util.Map;

public interface FightHistoryDAO {

    void saveFight(FightHistory fightHistory);
    List<FightHistory> getAllFights();
    List<FightHistory> getFightsByPlayerId(int playerId);
    List<Map<String, String>> getFightsByUsername(String username);
}
