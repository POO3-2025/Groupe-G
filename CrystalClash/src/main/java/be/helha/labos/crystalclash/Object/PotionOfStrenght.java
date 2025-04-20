package be.helha.labos.crystalclash.Object;

public class PotionOfStrenght extends ObjectBase {

    private int BonusATK;

    public PotionOfStrenght(String Name, int Price,int LevelRequired, int BonusATK ){
        super(Name,Price,LevelRequired,1);
        this.BonusATK = BonusATK;
    }

    @Override
    public String use() {
        if (!IsUsed()) return "Potion already used";
        Reducereliability();
        return "You won " + BonusATK + " in ATK";
    }

    public int getBonusATK(){
        return BonusATK;
    }

    public void setBonusATK(int BonusATK){
        this.BonusATK= BonusATK;
    }

    @Override
    public String toString() {
        return getName() + " – Dégâts : " + BonusATK;
    }
    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
                + "BonusATK : " + BonusATK + "\n";
    }
}
