package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.FightHistoryDAO;
import be.helha.labos.crystalclash.DTO.FightHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FightHistoryService {

    @Autowired
    private FightHistoryDAO fightHistoryDAO;

    public void saveFight(FightHistory fightHistory) {
        fightHistoryDAO.saveFight(fightHistory);
    }

    public List<FightHistory> getAllFights() {
      return   fightHistoryDAO.getAllFights();
    }
    public List<FightHistory>  getFightsByPlayerId(int playerId) {
        return fightHistoryDAO.getFightsByPlayerId(playerId);
    }

    public List<Map<String, String>> getFightsByUsername(String username) {
        return fightHistoryDAO.getFightsByUsername(username);
    }
}
