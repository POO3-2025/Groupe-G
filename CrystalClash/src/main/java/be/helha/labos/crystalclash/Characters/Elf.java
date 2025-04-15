package be.helha.labos.crystalclash.Characters;

public class Elf extends Personnage {
    public Elf(){
        super("Elf0",95,2);
    }

    @Override
    public void AttackSpecial(Personnage target){
        if(CanUseSpecialAttack()) target.receiveDamage(20);
    }

    @Override
    public boolean CanUseSpecialAttack(){
        return CompteurAttack >=4;
    }
}
