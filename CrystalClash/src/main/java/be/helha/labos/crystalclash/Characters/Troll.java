package be.helha.labos.crystalclash.Characters;

public class Troll extends Personnage {
    public Troll(){
        super("Troll",100,2);
    }
    @Override
    public void AttackSpecial(Personnage target){
        if(CanUseSpecialAttack()) target.receiveDamage(10);
        else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    @Override
    public boolean CanUseSpecialAttack(){
        return CompteurAttack >=3;
    }

}
