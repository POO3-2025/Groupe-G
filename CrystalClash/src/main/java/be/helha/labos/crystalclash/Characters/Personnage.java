package be.helha.labos.crystalclash.Characters;


public abstract class Personnage {

    private String Name;
    private int PV;
    private int AttackBase;
    protected int CompteurAttack = 0; // <- ici OK, accessible dans Dragon



    public Personnage(String Name, int PV, int AttackBase) {
        this.Name = Name;
        this.PV = PV;
        this.AttackBase = AttackBase;
    }

    public void tackle(Personnage target) {
        target.receiveDamage(AttackBase);
        CompteurAttack++;
    }

    public abstract void AttackSpecial(Personnage target);
    public abstract boolean CanUseSpecialAttack();

    public void receiveDamage(int damage) {
        this.PV -= damage;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public int getPV() {
        return PV;
    }

    public void setPV(int PV) {
        this.PV = PV;
    }

    public int getAttackBase() {
        return AttackBase;
    }

    public void setAttackBase(int attackBase) {
        this.AttackBase = attackBase;
    }

    public int getCompteurAttack() {
        return CompteurAttack;
    }

    public void setCompteurAttack(int compteurAttack) {
        this.CompteurAttack = compteurAttack;
    }


}
