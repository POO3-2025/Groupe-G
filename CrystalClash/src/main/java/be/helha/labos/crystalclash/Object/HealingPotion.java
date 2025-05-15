
package be.helha.labos.crystalclash.Object;

/**
 * Classe représentant une potion de soin.
 * La potion de soin permet de récupérer des points de vie.
 * Elle hérite de la classe ObjectBase.
 */
public class HealingPotion extends ObjectBase {
    /**
     * Points de vie récupérés par la potion.
     */
    private int heal;

    /**
     * Constructeur de la classe HealingPotion.
     * @param name
     * @param price
     * @param levelrequired
     * @param heal
     */
    public HealingPotion(String name, int price,int levelrequired, int heal){
        super(name,price,levelrequired,1);
        this.heal = heal;
    }

    /**
     * Utilise la potion de soin.
     * @return une chaîne de caractères décrivant le nombre de points de vie récupérés.
     */
    @Override
    public String use() {
        if (!IsUsed()) return "Potion already used";
        Reducereliability();
        return "You recovered " + heal + " PV";
    }
    /**
     * GETTER de la variable heal
     */
    public int getHeal(){
        return heal;
    }

    /**
     * SETTER de la variable heal
     */
    public void setHeal(int Heal){
        this.heal = Heal;
    }

    /**
     * toString de la classe HealingPotion
     */
    @Override
    public String toString() {
        return getName() + " – Pv : " + heal;
    }

    /**
     * Récupère le type de l'objet.
     * @return Le type de l'objet.
     */
    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
            + "Heal : " + heal + "\n";
    }
}
