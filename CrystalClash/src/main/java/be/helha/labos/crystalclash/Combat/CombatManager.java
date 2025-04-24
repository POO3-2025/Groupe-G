package be.helha.labos.crystalclash.Combat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {
    // Clé : joueur qui doit être notifié
    // Valeur : joueur qui l'a défié
    private static final Map<String, String> activeCombats = new ConcurrentHashMap<>();

    /**
     * Enregistre un combat entre deux joueurs.
     * @param adversaire Le joueur qui doit être notifié du combat.
     * @param initiateur Le joueur qui a lancé le combat.
     */
    public static void enregistrerCombat(String adversaire, String initiateur) {
        if (adversaire == null || initiateur == null) {
            throw new IllegalArgumentException("Les joueurs ne peuvent pas être nuls.");
        }
        activeCombats.put(adversaire, initiateur);
    }

    /**
     * Récupère et supprime le combat d'un joueur, indiquant ainsi qu'il est terminé.
     * @param joueur Le joueur pour lequel on veut récupérer le combat.
     * @return Le joueur qui a lancé le combat, ou null si aucun combat n'est trouvé.
     */
    public static String recupererCombat(String joueur) {
        if (joueur == null) {
            throw new IllegalArgumentException("Le joueur ne peut pas être nul.");
        }
        return activeCombats.remove(joueur);
    }

    /**
     * Vérifie si un joueur est engagé dans un combat.
     * @param joueur Le joueur à vérifier.
     * @return true si le joueur est dans un combat, false sinon.
     */
    public static boolean aUnCombat(String joueur) {
        if (joueur == null) {
            throw new IllegalArgumentException("Le joueur ne peut pas être nul.");
        }
        return activeCombats.containsKey(joueur);
    }
}

