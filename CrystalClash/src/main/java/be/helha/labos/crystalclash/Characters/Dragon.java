package be.helha.labos.crystalclash.Characters;

public class Dragon extends Personnage {

    public Dragon() {
        super("Dragon", 75, 10, "Coup de griffes ", 40, "Lance-flammes ",6);
    }

    @Override
    public void AttackSpecial(Personnage target) {
        if (CanUseSpecialAttack()) {
            target.receiveDamage(40);
        }else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    @Override
    public boolean CanUseSpecialAttack() {
        return CompteurAttack >= 6;
    }
}