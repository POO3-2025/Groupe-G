package be.helha.labos.crystalclash.User;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//Simple pour le moment
public class ConnectedUsers {
    private static final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    public static void addUser(String username) {
        connectedUsers.add(username);
    }

    public static void removeUser(String username) {
        connectedUsers.remove(username);
    }

    public static int getConnectedUserCount() {
        return connectedUsers.size();
    }

    public static Set<String> getConnectedUsers() {
        return connectedUsers;
    }
}
