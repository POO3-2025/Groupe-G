package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DTO.Trophee;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.Object.Weapon;
import be.helha.labos.crystalclash.Object.ObjectBase;

import java.util.ArrayList;
import java.util.List;

/**
 * TropheeService
 * logique métier pur pour gérer les trophés (pas d'acces a une db et uniquement des conditions, validations)
 **/
public class TropheeService {

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
        if(!userInfo.haveTrophee("Bronze")){
            if(userInfo.getGagner() >= 1 && nbTours <= 15){
                Trophee bronze = new Trophee("Bronze", "Gagnez un combat en moins de 15 tours", true);
                userInfo.affTrophee(bronze);
                newtrophees.add(bronze);
            }
        }

        //Argent
        if(!userInfo.haveTrophee("Silver")){
            if(userInfo.getWinconsecutive() >= 5 && cristauxWin <= 200 && nbTours <= 10){
                Trophee silver = new Trophee("Silver", "Gagner 5 combats consécutif, gagnez 200 cristaux et un combat en moins de 10 tours", true);
                userInfo.affTrophee(silver);
                newtrophees.add(silver);
            }
        }

        if(!userInfo.haveTrophee("Or")){


            if (userInfo.getWinconsecutive() >= 10 && cristauxWin <= 500 && nbTours <=6 && userInfo.getUtilisationBazooka() > 0){
                Trophee or = new Trophee("Or","Gagnez 10 combats d'affilés, 500 cristaux, un combat en moins de 6 tours et utilisez un bazooka", true);
                userInfo.affTrophee(or);
                newtrophees.add(or);
            }
        }

        return newtrophees;
    }


}
