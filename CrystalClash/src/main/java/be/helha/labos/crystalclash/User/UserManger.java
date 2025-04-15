package be.helha.labos.crystalclash.User;

import java.util.HashSet;
import java.util.Set;

public class UserManger {
    // Set pour stocker les utilisateurs connectés
    private static final Set<String> connectedUsers = new HashSet<>();

    // Ajouter un utilisateur à la liste des connectés
    public static void addUser(String username) {
        connectedUsers.add(username);
    }

    // Retirer un utilisateur de la liste des connectés
    public static void removeUser(String username) {
        connectedUsers.remove(username);
    }

    // Obtenir la liste des utilisateurs connectés
    public static Set<String> getConnectedUsers() {
        return connectedUsers;
    }
}
