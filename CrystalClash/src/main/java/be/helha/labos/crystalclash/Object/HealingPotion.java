package be.helha.labos.crystalclash.Object;

public class HealingPotion extends ObjectBase {

    private int Heal;

    public HealingPotion(String Name, int Price,int LevelRequired, int Heal){
        super(Name,Price,LevelRequired,1);
        this.Heal = Heal;
    }

    @Override
    public String use() {
        if (!IsUsed()) return "Potion already used";
        Reducereliability();
        return "You recovered " + Heal + " PV";
    }

    public int getHeal(){
        return Heal;
    }

    public void setHeal(int Heal){
        this.Heal = Heal;
    }
    @Override
    public String toString() {
        return getName() + " – Dégâts : " + Heal;
    }
}
