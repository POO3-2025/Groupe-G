package be.helha.labos.crystalclash.Object;

import java.util.ArrayList;
import java.util.List;

public class CoffreDesJoyaux extends ObjectBase {
    private List<ObjectBase> contenu;
    private final int CAPACITE_MAX = 10;

    public CoffreDesJoyaux() {
        super("Coffre des Joyaux", 25, 1, 1);


        this.contenu = new ArrayList<>();
    }

    @Override
    public String use() {
        if (!IsUsed()) return "The chest has already been opened.";
        Reducereliability();
        return "You open the Coffre des Joyaux Chest and discover" + contenu.size() + " objets !";
    }

    public boolean AddObjects(ObjectBase object) {
        if (contenu.size() >= CAPACITE_MAX) return false;
        contenu.add(object);
        return true;
    }


    public List<ObjectBase> getContenu() {
        return contenu;
    }

    public int getMaxCapacity() {
        return CAPACITE_MAX;
    }
    public int setCapaciteMax(int maxCapacity) {
        return this.CAPACITE_MAX;
    }

    public void setContenu(List<ObjectBase> contenu) {
        this.contenu = contenu;
    }

}
