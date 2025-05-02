package be.helha.labos.crystalclash.Service;


import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Object.*;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class FightService {

    //Pailre clés valeurs (String id ou nom et StateCombat l'etat du combat)
    private final Map<String, StateCombat> combats = new HashMap<>();


    public void createCombat(String p1, String p2, Personnage char1, Personnage char2,
                             List<ObjectBase> bp1, List<ObjectBase> bp2) {
        StateCombat state = new StateCombat(p1, p2, char1, char2, bp1, bp2);
        combats.put(p1, state);
        combats.put(p2, state);
    }

    public StateCombat getCombat(String username) {
        return combats.get(username);
    }

    public void HandleAttach(String Player, String type) {
        StateCombat state = combats.get(Player);
        if (state == null || !state.getPlayerNow().equals(Player)) {
            return;
        }

        Personnage attack = state.getCharacter(Player);
        String oppenent = state.getOpponent(Player);
        int Damage;

        if (type.equals("normal")) {
            Damage = attack.getAttackBase();
            attack.CompteurAttack(attack.getCompteurAttack() + 1);
            state.addLog(Player + " utilise " + attack.getNameAttackBase() + " (" + Damage + " damage)");
        }else{
            if(attack.getCompteurAttack() < attack.getRestrictionAttackSpecial()){
                state.addLog(Player + " ne peut pas utiliser son attaque spéciale !");
                return;
            }
            Damage = attack.getAttackSpecial();
            state.addLog(Player + " utilise " + attack.getNameAttaqueSpecial() + " (" + Damage + " damage)");
        }

        int newPv = state.getPv(oppenent) - Damage;
        state.setPv(oppenent, newPv);
        state.NextTurn();
    }

    public void useObject(String Player, String objectId){
        StateCombat state = combats.get(Player);
        if (state == null || !state.getPlayerNow().equals(Player)) {
            return;
        }

        List<ObjectBase> backpack = state.getBackpack(Player);
        Optional<ObjectBase> objet = backpack.stream().filter(o -> o.getId().equals(objectId)).findFirst();
        if(objet.isEmpty())return;

        ObjectBase obj = objet.get();
        Personnage perso = state.getCharacter(Player);

        if(obj instanceof Weapon){
            int dmg = ((Weapon) obj).getDamage();
        String oppenent = state.getOpponent(Player);
        int NewPv = state.getPv(oppenent) - dmg;
        state.setPv(oppenent, NewPv);
        state.addLog(Player + " utilise " + obj.getName() + " et inflige " + dmg + " dégâts !");
        }else
        if(obj instanceof HealingPotion){
            int pv = state.getPv(Player);
            int heal = ((HealingPotion) obj).getHeal();
            state.setPv(Player, heal);
            state.addLog(Player + " boit une potion de soin (+ " + heal + " PV)");
        }else
        if(obj instanceof Armor){
            int bonus = ((Armor) obj).getBonusPV();
            int pv = state.getPv(Player);
            state.setPv(Player, pv + bonus);
            state.addLog(Player + " utilise " + obj.getName() + " et gagne " + bonus + " PV temporairement");
        }

        obj.Reducereliability();
        if (!obj.IsUsed()) {
            backpack.remove(obj); // Supprime si fiabilité 0
        }

        state.NextTurn();
    }

}