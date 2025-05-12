package be.helha.labos.crystalclash.Object;

public class Armor extends ObjectBase {
    private int bonusPV;

    public Armor(String name, int price, int levelrequired,int reliability, int bonusPV) {
        super(name, price, levelrequired, reliability);
        this.bonusPV = bonusPV;
    }


    @Override
    public String use() {
        if (!IsUsed()) return "The Armor worn";
        Reducereliability();
        return "Armor used , +" + bonusPV + "Pv during " +reliability+ " tours.";
    }

    public int getBonusPV() {
        return bonusPV;
    }



    public void setBonusPV(int bonusPV) {
        bonusPV = bonusPV;
    }

    @Override
    public String toString() {
        return getName() + " – Dégâts : " + bonusPV;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
                + "Défense : " + bonusPV + "\n";
    }
}
