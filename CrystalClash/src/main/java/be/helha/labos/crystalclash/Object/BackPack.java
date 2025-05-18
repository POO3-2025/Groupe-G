package be.helha.labos.crystalclash.Object;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant un sac à dos.
 * Le sac à dos a une capacité maximale de 5 objets.
 * Il peut contenir des objets de type ObjectBase.
 */
public class BackPack {
    /**
     * Capacité maximale du sac à dos.
     */
    private final int CAPACITE_MAX = 5;
    /**
     * Liste des objets contenus dans le sac à dos.
     */
    private List<ObjectBase> objets;

    /**
     * Constructeur de la classe BackPack.
     * Initialise la liste des objets.
     */
    public BackPack() {
        this.objets = new ArrayList<>();
    }

    /**
     * Ajoute un objet au sac à dos.
     * @param objet L'objet à ajouter.
     * @return true si l'objet a été ajouté, false si le sac est plein.
     */
    public boolean AddObjects(ObjectBase objet) {
        if (objets.size() >= CAPACITE_MAX) return false;
        objets.add(objet);
        return true;
    }

    /**
     * Retire un objet du sac à dos.
     * @param objet L'objet à retirer.
     * @return true si l'objet a été retiré, false sinon.
     */
    public boolean removeObject(ObjectBase objet) {
        return objets.remove(objet);
    }

    /**
     * Récupère la liste des objets contenus dans le sac à dos.
     * @return objects Liste des objets.
     */
    public List<ObjectBase> getObjets() {
        return objets;
    }

    /**
     *
     * @param objets la nouvelle liste d'objets à affecter au personnage ou à l'utilisateur
     */
    //Utilsié dans figthService pour changer l'etat de l'endurance de l'objet
    public void setObjets(List<ObjectBase> objets) {
        this.objets = objets;
    }

    /**
     * Récupère la capacité maximale du sac à dos.
     * @return CAPACITE_MAX La capacité maximale.
     */
    public int getCapaciteMax() {
        return CAPACITE_MAX;
    }
}
