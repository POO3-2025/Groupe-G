package be.helha.labos.crystalclash.Object;

public class Weapon extends ObjectBase {

    private int damage;

    public Weapon(String Name, int Price, int LevelRequired, int reliability, int Damage) {
        super(Name, Price, LevelRequired, reliability);
        this.damage = Damage;
    }

    @Override
    public String use() {
        if (!IsUsed()) return "The weapon is broken";
        Reducereliability();
        return "The weapon deal " + damage + " damage";
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
