package be.helha.labos.crystalclash.Characters;

public class Aquaman extends Personnage {

    public Aquaman(){
        super("Aquaman",85,10);
    }

    @Override
    public void AttackSpecial(Personnage target){
        if(CanUseSpecialAttack()) target.receiveDamage(30);
        else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    @Override
    public boolean CanUseSpecialAttack(){
        return CompteurAttack >=5;
    }

}
