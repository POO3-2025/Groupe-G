package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.User.UserInfo;

import java.util.List;
/**
 * Interface définissant les opérations d'accès aux données
 * liées aux combats entre joueurs.
 */
public interface FightDAO {

   /**
    * Récupère la liste des joueurs classés selon leurs performances en combat.
    * Le classement est généralement basé sur des critères comme le nombre de victoires,
    * le score ou tout autre système défini dans la logique métier.
    * @return une liste d'objets représentant les joueurs triés selon leur classement
    */
   List<UserInfo> getClassementPlayer();
}
