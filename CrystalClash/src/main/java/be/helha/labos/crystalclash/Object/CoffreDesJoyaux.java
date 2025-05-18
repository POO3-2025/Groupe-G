package be.helha.labos.crystalclash.Object;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant un coffre contenant des objets.
 * Le coffre a une capacité maximale de 10 objets.
 * Il peut contenir des objets de type ObjectBase.
 */
public class CoffreDesJoyaux extends ObjectBase {
    /**
     * Liste des objets contenus dans le coffre.
     * Capacité maximale du coffre.
     */
    private List<ObjectBase> contenu;
    private final int CAPACITE_MAX = 10;

    /**
     * Constructeur de la classe CoffreDesJoyaux.
     * Initialise un coffre prédéfini avec les valeurs suivantes :
     * nom : "Coffre des Joyaux", prix : 25, niveau requis : 1, fiabilité : 50.
     * La liste des objets contenus dans le coffre est également initialisée à vide.
     */
    public CoffreDesJoyaux() {
        super("Coffre des Joyaux", 25, 1, 50);
        this.contenu = new ArrayList<>();
    }

    /**
     * Utilise le coffre
     * @return une chaîne de caractères décrivant le nombre d'objets découverts dans le coffre
     */
    @Override
    public String use() {
        return "You open the Coffre des Joyaux Chest and discover" + contenu.size() + " objets !";
    }

    /**
     * Ajoute un objet au coffre.
     * @param object object
     * @return true si l'objet a été ajouté, false si le coffre est plein.
     */
    public boolean AddObjects(ObjectBase object) {
        if (contenu.size() >= CAPACITE_MAX) return false;
        contenu.add(object);
        return true;
    }

    /**
     * recupere le contenu du coffre.
     * @return contenu
     */
    public List<ObjectBase> getContenu() {
        return contenu;
    }

    /**
     * Récupère la capacité maximale du coffre.
     * @return la capacité maximale du coffre
     */
    public int getMaxCapacity() {
        return CAPACITE_MAX;
    }
    /**
     * Set la capacité maximale du coffre.
     * @return la capacité maximale du coffre
     */
    public int  setCapaciteMax() {
        return CAPACITE_MAX;
    }

    /**
     * Set la liste des objets contenus dans le coffre.
     * @param contenu
     */
    public void setContenu(List<ObjectBase> contenu) {
        this.contenu = contenu;
    }

}
