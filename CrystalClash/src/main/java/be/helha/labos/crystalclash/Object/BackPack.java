package be.helha.labos.crystalclash.Object;

import java.util.ArrayList;
import java.util.List;

public class BackPack {
    private final int CAPACITE_MAX = 5;
    private List<ObjectBase> objets;

    public BackPack() {
        this.objets = new ArrayList<>();
    }

    public boolean AddObjects(ObjectBase objet) {
        if (objets.size() >= CAPACITE_MAX) return false;
        objets.add(objet);
        return true;
    }

    public boolean removeObject(ObjectBase objet) {
        return objets.remove(objet);
    }

    public List<ObjectBase> getObjets() {
        return objets;
    }

    //Utilsié dans figthService pour changer l'etat de l'endurance de l'objet
    public void setObjets(List<ObjectBase> objets) {
        this.objets = objets;
    }

    public int getCapaciteMax() {
        return CAPACITE_MAX;
    }
}
