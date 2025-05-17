package be.helha.labos.crystalclash.Object;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant un équipement.
 * L'équipement peut contenir des armures.
 * Il hérite de la classe ObjectBase.
 * Il a une capacité maximale d'une armure.
 */
public class Equipment {
    /**
     * Capacité maximale d'armures que l'équipement peut contenir.
     * Par défaut, une seule armure peut être équipée.
     */
    private final int CAPACITE_MAX = 1;

    /**
     * Liste des armures équipées.
     */
    private List<ObjectBase> armorList;

    /**
     * Constructeur de la classe Equipment.
     * Initialise la liste d'armures vide.
     */
    public Equipment() {
        this.armorList = new ArrayList<>();
    }

    /**
     * Tente d'ajouter une armure à l'équipement.
     * @param armure l'objet représentant l'armure à ajouter
     * @return true si l'armure a été ajoutée,  false si la capacité est atteinte
     */
    public boolean AddArmor(ObjectBase armure) {
        if (armorList.size() >= CAPACITE_MAX) return false;
        armorList.add(armure);
        return true;
    }

    /**
     * Retire une armure de l'équipement.
     * @param armure l'objet représentant l'armure à retirer
     * @return true si l'armure a été retirée,false sinon
     */
    public boolean removeArmor(ObjectBase armure) {
        return armorList.remove(armure);
    }

    /**
     * Récupère la liste des armures équipées.
     * @return armorList Liste des armures.
     */
    public List<ObjectBase> getObjets() {
        return armorList;
    }

    /**
     * Récupère la liste des armures équipées.
     * return armorList Liste des armures.
     * @param armorList etat objet
     */
    //Utilsié dans figthService pour changer l'etat de l'endurance de l'objet
    public void setObjets(List<ObjectBase> armorList) {
        this.armorList = armorList;
    }

    /**
     * Récupère la capacité maximale de l'équipement.
     * @return la capacité maximale
     */
    public int getCapaciteMax() {
        return CAPACITE_MAX;
    }
}
