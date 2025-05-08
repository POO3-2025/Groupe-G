package be.helha.labos.crystalclash.Object;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Equipment {
    private final int CAPACITE_MAX = 1;


    private List<ObjectBase> armorList;

    public Equipment() {
        this.armorList = new ArrayList<>();
    }

    public boolean AddArmor(ObjectBase armure) {
        if (armorList.size() >= CAPACITE_MAX) return false;
        armorList.add(armure);
        return true;
    }

    public boolean removeArmor(ObjectBase armure) {
        return armorList.remove(armure);
    }

    public List<ObjectBase> getObjets() {
        return armorList;
    }

    //Utilsié dans figthService pour changer l'etat de l'endurance de l'objet
    public void setObjets(List<ObjectBase> armorList) {
        this.armorList = armorList;
    }

    public int getCapaciteMax() {
        return CAPACITE_MAX;
    }
}
