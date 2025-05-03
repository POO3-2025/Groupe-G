
package be.helha.labos.crystalclash.Object;

public class HealingPotion extends ObjectBase {

    private int heal;

    public HealingPotion(String name, int price,int levelrequired, int heal){
        super(name,price,levelrequired,1);
        this.heal = heal;
    }

    @Override
    public String use() {
        if (!IsUsed()) return "Potion already used";
        Reducereliability();
        return "You recovered " + heal + " PV";
    }

    public int getHeal(){
        return heal;
    }

    public void setHeal(int Heal){
        this.heal = Heal;
    }
    @Override
    public String toString() {
        return getName() + " – Pv : " + heal;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + "\n"
            + "Heal : " + heal + "\n";
    }
}
