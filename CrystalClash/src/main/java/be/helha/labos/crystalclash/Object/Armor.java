package be.helha.labos.crystalclash.Object;

/**
 * Classe représentant une armure.
 * L'armure hérite de la classe ObjectBase.
 * Elle confère un bonus de points de vie au joueur lorsqu'elle est utilisée.
 */
public class Armor extends ObjectBase {
    /**
     * Le bonus de points de vie conféré par l'armure.
     */
    private int bonusPV;
    /**
     * Constructeur principal de l'armure.
     *
     * @param name          le nom de l'armure
     * @param price         le prix de l'armure
     * @param levelrequired le niveau requis pour l'utiliser
     * @param reliability   la durabilité de l'objet (nombre d’utilisations)
     * @param bonusPV       le bonus de points de vie conféré
     */
    public Armor(String name, int price, int levelrequired,int reliability, int bonusPV) {
        super(name, price, levelrequired, reliability);
        this.bonusPV = bonusPV;
        this.setType("Armor");
    }

    /**
     * Utilise l'armure.
     * Si elle n’a pas encore été utilisée, elle est portée.
     * Sinon, elle diminue en durabilité et applique son bonus.
     * @return un message décrivant l'effet de l'utilisation
     */
    @Override
    public String use() {
        if (!IsUsed()) return "The Armor worn";
        Reducereliability();
        return "Armor used , +" + bonusPV + "Pv during " +reliability+ " tours.";
    }
    /**
     * Récupère le bonus de points de vie. get
     * @return le bonus de points de vie
     */
    public int getBonusPV() {
        return bonusPV;
    }

    /**
     * Tostring de l'armure.
     */
    @Override
    public String toString() {
        return getName() + " – Dégâts : " + bonusPV;
    }
    /**
     * Retourne les détails complets de l'objet.
     * @return description détaillée de l’armure
     */
    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
                + "Défense : " + bonusPV + "\n";
    }



}
