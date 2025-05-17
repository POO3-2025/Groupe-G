package be.helha.labos.crystalclash.DTO;

import be.helha.labos.crystalclash.Object.ObjectBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui gere l'inventaire d'un joueur
 * Elle contient une liste d'objets et une capacite max
 * Elle permet d'ajouter et de retirer des objets
 * Elle permet de savoir si l'inventaire est plein
 * Elle permet de savoir combien d'objets il y a dans l'inventaire
 */
public class Inventory {
    private final int CAPACITE_MAX = 30;
    private List<ObjectBase> objets;
    private String username; //Pour ne pas etre ingoré de conversion json

    /**
     * Constructeur de la classe Inventory
     * Il initialise la liste d'objets
     */
    public Inventory() {
        this.objets = new ArrayList<>();
    }

    /**
     * Ajouter des objets a l'inventaire
    * @param objet objet
     * @return true si l'objet a été ajouté, false sinon
     * Si l'inventaire pas plein on ajoute l objet dedans
    * **/
    public boolean ajouterObjet(ObjectBase objet) {
        if (objets.size() >= CAPACITE_MAX) return false;
        objets.add(objet);
        return true;
    }

    /**
     * Retirer un objet de l'inventaire
     * @param objet objet
     * @return true si l'objet a été retiré, false sinon
     * **/
    public boolean retirerObjet(ObjectBase objet) {
        return objets.remove(objet);
    }

    /**
     * Recuperer la liste d'objets
     * @return objets
     * **/
    public List<ObjectBase> getObjets() {
        return objets;
    }

    /**
     * Recuperer la taille de l'inventaire
     * @return la taille de l'inventaire
     * **/
    public int getCapaciteMax() {
        return CAPACITE_MAX;
    }

    /**
     *Cette méthode peut être utilisée, par exemple, pour mettre à jour l'état
     *  des objets après un événement comme un combat, une sauvegarde, ou un chargement.
     * @param objets
     * **/
    public void setObjets(List<ObjectBase> objets) {
        this.objets = objets;
    }

    /**
     * @return username
     * Permet de recuperer le nom d'utilisateur de l'inventaire
     * **/
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     * Change le nom d'utilisateur de l'inventaire
     * **/
    public void setUsername(String username) {
        this.username = username;
    }
}
