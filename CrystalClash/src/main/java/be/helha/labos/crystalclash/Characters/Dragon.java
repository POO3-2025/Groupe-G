package be.helha.labos.crystalclash.Characters;

public class Dragon extends Personnage{
    public Dragon(){
        super("Drangon",75,10);
    }

    @Override
    public void AttackSpecial(Personnage target){
        if(CanUseSpecialAttack()) target.receiveDamage(40);
    }

    @Override
    public boolean CanUseSpecialAttack(){
        return CompteurAttack >=6;
    }
}
