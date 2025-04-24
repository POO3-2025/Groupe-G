package be.helha.labos.crystalclash.Service;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class CombatService {

    // Set pour maintenir les joueurs en attente
    private Set<String> playersWaitingForCombat = new HashSet<>();

    public String lancerCombat(String username) {
        // Ajouter le joueur dans la liste des en attente
        playersWaitingForCombat.add(username);

        // Vérifier s'il y a un adversaire disponible
        for (String waitingPlayer : playersWaitingForCombat) {
            if (!waitingPlayer.equals(username)) {
                // Si un adversaire est trouvé, on lance le combat
                playersWaitingForCombat.remove(waitingPlayer);
                return "Combat lancé entre " + username + " et " + waitingPlayer + " !";
            }
        }

        // Si aucun adversaire n'est trouvé, on indique qu'on attend un adversaire
        return "Combat lancé pour " + username + " ! En attente d’un adversaire...";
    }
}
