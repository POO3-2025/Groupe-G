
package be.helha.labos.crystalclash.Object;

/**
 * Classe représentant une arme.
 * L'arme inflige des dégâts au joueur.
 * Elle hérite de la classe ObjectBase.
 */
public class Weapon extends ObjectBase {
    /**
     * Dégâts infligés par l'arme.
     */
    private int damage;

    /**
     * Constructeur de la classe Weapon.
     * @param name
     * @param price
     * @param levelrequired
     * @param reliability
     * @param damage
     */
    public Weapon(String name, int price, int levelrequired, int reliability, int damage) {
        super(name, price, levelrequired, reliability);
        this.damage = damage;
    }

    /**
     * Utilise l'arme.
     * @return une chaîne de caractères décrivant les dégâts infligés par l'arme.
     */
    @Override
    public String use() {
        if (!IsUsed()) return "The weapon is broken";
        Reducereliability();
        return "The weapon deal " + damage + " damage";
    }
    /**
     * GETTER de la variable damage
     */
    public int getDamage() {
        return damage;
    }
    /**
     * SETTER de la variable damage
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }
    /**
     * toString de la classe Weapon
     */
    @Override
    public String toString() {
        return getName() + " – Dégâts : " + damage;
    }
    /**
     * Récupère le type de l'objet.
     * @return le type de l'objet
     */
    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
            + "Dégâts : " + damage + "\n";
    }
}
