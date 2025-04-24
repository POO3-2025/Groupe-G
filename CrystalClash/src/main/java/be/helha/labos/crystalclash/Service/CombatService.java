package be.helha.labos.crystalclash.Service;

import org.springframework.stereotype.Service;

@Service
public class CombatService {
    public String lancerCombat(String username) {
        // Logique pour appairer un joueur, ou gérer l’attente
        return "Combat lancé pour " + username + " ! En attente d’un adversaire...";
    }
}
