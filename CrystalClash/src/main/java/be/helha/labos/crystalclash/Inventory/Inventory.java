package be.helha.labos.crystalclash.Inventory;

import be.helha.labos.crystalclash.Object.ObjectBase;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final int CAPACITE_MAX = 30;
    private List<ObjectBase> objets;

    public Inventory() {
        this.objets = new ArrayList<>();
    }

    public boolean ajouterObjet(ObjectBase objet) {
        if (objets.size() >= CAPACITE_MAX) return false;
        objets.add(objet);
        return true;
    }

    public boolean retirerObjet(ObjectBase objet) {
        return objets.remove(objet);
    }

    public List<ObjectBase> getObjets() {
        return objets;
    }

    public int getCapaciteMax() {
        return CAPACITE_MAX;
    }
}
