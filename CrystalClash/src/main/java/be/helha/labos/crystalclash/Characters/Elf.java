package be.helha.labos.crystalclash.Characters;

public class Elf extends Personnage {
    public Elf(){
        super("Elf",95,2);
    }

    @Override
    public void AttackSpecial(Personnage target){
        System.out.println("Pluie de flèches ! " + getName() + " inflige 20 PV");
        if(CanUseSpecialAttack()) target.receiveDamage(20);
        else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    @Override
    public boolean CanUseSpecialAttack(){
        return CompteurAttack >=4;
    }
}
