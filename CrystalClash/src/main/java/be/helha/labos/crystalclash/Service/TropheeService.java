package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.UserCombatStatDAO;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.DTO.Trophee;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.Object.Weapon;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

/**
 * TropheeService
 * logique métier pur pour gérer les trophés (pas d'acces a une db et uniquement des conditions, validations)
 **/
@Service
public class TropheeService {

    @Autowired
    private UserCombatStatService userCombatStatService;

    @Autowired
    private InventoryService inventoryService;

    /**
     * @param cristauxWin  -> nbr de cristaux obtenus lors des combats
     * @param nbTours -> durée tours du combat
     * @param objectUse -> savoir si le user a use le bazooka durant le combat
     * @param userInfo -> infos neccesaire du user
     * List pour retourne tous les trophés débloqués lors d'un comnbat joué
     * **/
    public List<Trophee> getTrophees(UserInfo userInfo, int cristauxWin, int nbTours,List<ObjectBase> objectUse) {
        List<Trophee> newtrophees = new ArrayList<>();

        //Bronze
        if(!userInfo.haveTrophee("bronze")){
            if(userInfo.getGagner() >= 1 && nbTours <= 15){
                Trophee bronze = new Trophee("bronze", "Gagnez un combat en moins de 15 tours");
                bronze.debloquer();
                userInfo.affTrophee(bronze);

                userCombatStatService.updateStatsTrophy(userInfo.getUsername(),"bronze");

                ObjectBase recompense = ObjectFactory.CreateObject("epee en bois", "Weapon", userInfo.getLevel());

                Inventory inv = inventoryService.getInventoryForUser(userInfo.getUsername());
                inv.ajouterObjet(recompense);
                inventoryService.saveInventoryForUser(userInfo.getUsername(), inv);

                newtrophees.add(bronze);
            }
        }

        //Argent
        if(!userInfo.haveTrophee("silver")){
            if(userInfo.getWinconsecutive() >= 5 && cristauxWin >= 200 && nbTours <= 10){
                Trophee silver = new Trophee("silver", "Gagner 5 combats consécutif, gagnez 200 cristaux et un combat en moins de 10 tours");
                silver.debloquer();
                userInfo.affTrophee(silver);
                userCombatStatService.updateStatsTrophy(userInfo.getUsername(),"silver");
                ObjectBase recompense = ObjectFactory.CreateObject("couteau en diamant", "Weapon", userInfo.getLevel());
                Inventory inv = inventoryService.getInventoryForUser(userInfo.getUsername());
                inv.ajouterObjet(recompense);
                inventoryService.saveInventoryForUser(userInfo.getUsername(), inv);

                newtrophees.add(silver);
            }
        }

        if(!userInfo.haveTrophee("or")){


            if (userInfo.getWinconsecutive() >= 10 && cristauxWin >= 500 && nbTours <=6 && userInfo.getUtilisationBazooka() > 0){
                Trophee or = new Trophee("or","Gagnez 10 combats d'affilés, 500 cristaux, un combat en moins de 6 tours et utilisez un bazooka");
                or.debloquer();
                userInfo.affTrophee(or);
                userCombatStatService.updateStatsTrophy(userInfo.getUsername(),"or");
                ObjectBase recompense = ObjectFactory.CreateObject("bazooka", "Weapon", userInfo.getLevel());
                Inventory inv = inventoryService.getInventoryForUser(userInfo.getUsername());
                inv.ajouterObjet(recompense);
                inventoryService.saveInventoryForUser(userInfo.getUsername(), inv);
                newtrophees.add(or);
            }
        }

        return newtrophees;
    }


    /**
     * Pour les tests
     *
     * **/
    public void setUserCombatStatService(UserCombatStatService userCombatStatService) {
        this.userCombatStatService = userCombatStatService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

}
