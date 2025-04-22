package be.helha.labos.crystalclash.Inventory;

import be.helha.labos.crystalclash.Object.ObjectBase;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final int CAPACITE_MAX = 30;
    private List<ObjectBase> objets;
    private String username; //Pour ne pas etre ingoré de conversion json

    public Inventory() {
        this.objets = new ArrayList<>();
    }

    /**
    * @param objet
     * Si l'inventaire pas plein on ajoute l objet dedans
    * **/
    public boolean ajouterObjet(ObjectBase objet) {
        if (objets.size() >= CAPACITE_MAX) return false;
        objets.add(objet);
        return true;
    }

    /**
     * @param objet
     * **/
    public boolean retirerObjet(ObjectBase objet) {
        return objets.remove(objet);
    }

    public List<ObjectBase> getObjets() {
        return objets;
    }

    public int getCapaciteMax() {
        return CAPACITE_MAX;
    }

    /**
     * @param objets
     * **/
    public void setObjets(List<ObjectBase> objets) {
        this.objets = objets;
    }

    public String getUsername() {
        return username;
    }

    /**
     * @param username
     * Permet de savoir a qui linventaire apparatient
     * **/
    public void setUsername(String username) {
        this.username = username;
    }
}
