package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.DTO.FightHistory;
import java.util.List;

public interface FightHistoryDAO {

    void saveCombat(FightHistory history);
    List<FightHistory> findCombatsByUser(String username);

}
