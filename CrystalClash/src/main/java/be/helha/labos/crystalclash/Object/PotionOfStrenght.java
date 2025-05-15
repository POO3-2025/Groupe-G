
package be.helha.labos.crystalclash.Object;

/**
 * Classe représentant une potion de force.
 * La potion de force permet d'augmenter les dégâts infligés par le joueur.
 * Elle hérite de la classe ObjectBase.
 */
public class PotionOfStrenght extends ObjectBase {
    /**
     * Dégâts supplémentaires infligés par la potion.
     */
    private int bonusATK;

    /**
     * Constructeur de la classe PotionOfStrenght.
     * @param name
     * @param price
     * @param levelrequired
     * @param bonusATK
     */
    public PotionOfStrenght(String name, int price,int levelrequired, int bonusATK ){
        super(name,price,levelrequired,1);
        this.bonusATK = bonusATK;
    }

    /**
     * Utilise la potion de force.
     * @return une chaîne de caractères décrivant le nombre de dégâts supplémentaires infligés.
     */
    @Override
    public String use() {
        if (!IsUsed()) return "Potion already used";
        Reducereliability();
        return "You won " + bonusATK + " in ATK";
    }
    /**
     * GETTER de la variable bonusATK
     */
    public int getBonusATK(){
        return bonusATK;
    }
    /**
     * SETTER de la variable bonusATK
     */
    public void setBonusATK(int BonusATK){
        this.bonusATK= BonusATK;
    }
    /**
     * toString de la classe PotionOfStrenght
     */
    @Override
    public String toString() {
        return getName() + " – Dégâts : " + bonusATK;
    }

    /**
     * Récupère le type de l'objet.
     * @return
     */
    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
            + "BonusATK : " + bonusATK + "\n";
    }
}
