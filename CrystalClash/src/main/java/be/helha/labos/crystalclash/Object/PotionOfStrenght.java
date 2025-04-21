
package be.helha.labos.crystalclash.Object;

public class PotionOfStrenght extends ObjectBase {

    private int bonusATK;

    public PotionOfStrenght(String name, int price,int levelrequired, int bonusATK ){
        super(name,price,levelrequired,1);
        this.bonusATK = bonusATK;
    }

    @Override
    public String use() {
        if (!IsUsed()) return "Potion already used";
        Reducereliability();
        return "You won " + bonusATK + " in ATK";
    }

    public int getBonusATK(){
        return bonusATK;
    }

    public void setBonusATK(int BonusATK){
        this.bonusATK= BonusATK;
    }

    @Override
    public String toString() {
        return getName() + " – Dégâts : " + bonusATK;
    }
    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
            + "BonusATK : " + bonusATK + "\n";
    }
}
