package be.helha.labos.crystalclash.Characters;

import be.helha.labos.Inventory.*;
import be.helha.labos.Object.*;
public abstract class Personnage {

    private String Name;
    private int PV;
    private int AttackBase;
    protected int CompteurAttack = 0;

    private BackPack backpack = new BackPack();

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

    public void AttackBase(int AttackBase) {
        this.AttackBase = AttackBase;
    }

    public int getCompteurAttack() {
        return CompteurAttack;
    }

    public void CompteurAttack(int CompteurAttack) {
        this.CompteurAttack = CompteurAttack;
    }

    public BackPack getBackpack() {
        return backpack;
    }
}
