package be.helha.labos.crystalclash.Object;

public class Armor extends ObjectBase {
    private int BonusPV;

    public Armor(String Name, int Price, int LevelRequired,int reliability, int BonusPV) {
        super(Name, Price, LevelRequired, reliability);
        this.BonusPV = BonusPV;
    }


    @Override
    public String use() {
        if (!IsUsed()) return "The weapon worn";
        Reducereliability();
        return "Armor used , +" + BonusPV + "Pv during " +reliability+ " tours.";
    }

    public int getBonusPV() {
        return BonusPV;
    }

    public void setBonusPV(int bonusPV) {
        BonusPV = bonusPV;
    }

    @Override
    public String toString() {
        return getName() + " – Dégâts : " + BonusPV;
    }
}
