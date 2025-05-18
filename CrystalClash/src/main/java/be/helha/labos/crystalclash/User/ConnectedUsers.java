package be.helha.labos.crystalclash.User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConnectedUsers map qui va contenir tous les users co
 * **/
public class ConnectedUsers {
    //Ici une map qui contien les infos du joeuur connecté
    private static final Map<String,UserInfo> connectedUsers = new ConcurrentHashMap<>();

    /**
     * Ajoute un utilisateur à la liste des connectés.
     * @param userInfo informations complètes de l'utilisateur
     */
    public static void addUser(UserInfo userInfo) {
        connectedUsers.put(userInfo.getUsername(),userInfo);
    }

    /**
     * Supprime un utilisateur de la liste.
     * @param username nom de l'utilisateur
     */
    public static void removeUser(String username) {
        connectedUsers.remove(username);
    }

    /**
     * Retourne le nombre d'utilisateurs connectés.
     * @return connectedUsers.size()
     */
    public static int getConnectedUserCount() {
        return connectedUsers.size();
    }

    /**
     * Récupère un utilisateur connecté par son nom.
     * @return connectedUsers.get(username);
     * @param username  username
     */
    public static UserInfo getUser(String username) {
        return connectedUsers.get(username);
    }
    /**
     * @return connectedUsers
     * Retourne une Map contenant les infos du joueur
     */
    public static Map<String, UserInfo> getConnectedUsers() {
        return connectedUsers;
    }
}
