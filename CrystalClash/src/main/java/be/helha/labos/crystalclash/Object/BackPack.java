package be.helha.labos.crystalclash.Object;

import java.util.ArrayList;
import java.util.List;

public class BackPack {
    private final int CAPACITE_MAX = 5;
    private List<ObjectBase> objets;

    public BackPack() {
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

