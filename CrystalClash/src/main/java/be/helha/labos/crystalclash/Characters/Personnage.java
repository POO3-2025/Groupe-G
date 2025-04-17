package be.helha.labos.crystalclash.Characters;

import be.helha.labos.crystalclash.Object.BackPack;

public class Personnage {

    private String Name;
    private int PV;
    private int AttackBase;
    protected int CompteurAttack = 0;

    private BackPack backpack = new BackPack();

    public Personnage() {
        // Requis pour désérialisation JSON en hson ou jackson
    }

    public Personnage(String Name, int PV, int AttackBase) {
        this.Name = Name;
        this.PV = PV;
        this.AttackBase = AttackBase;
    }

    public void tackle(Personnage target) {
        System.out.println(this.Name + " attaque " + target.getName() + " avec une attaque normale !");
        target.receiveDamage(AttackBase);
        CompteurAttack++;
    }

    public  void AttackSpecial(Personnage target){
        System.out.println(Name + " n’a pas d’attaque spéciale ou ne peut pas l’utiliser.");
    };

    public  boolean CanUseSpecialAttack(){
return false;
    };

    public void receiveDamage(int damage) {
        this.PV -= damage;
        System.out.println(Name + " subit " + damage + " points de dégâts. PV restants : " + PV);
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

    public void AttackBase(int AttackBase) {
        this.AttackBase = AttackBase;
    }

    public int getCompteurAttack() {
        return CompteurAttack;
    }

    public void CompteurAttack(int CompteurAttack) {
        this.CompteurAttack = CompteurAttack;
    }

}
