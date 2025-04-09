package be.helha.labos.crystalclash.server_auth;

/*
 *Stocker en local les infos du joueur connecté pendant que le jeu tourne
 * garde le token jwt que le serv sping envoie apres le login
 * garde le nom du joueur
 * permet de vérifier si 1 joueur est connecté ou non
 * et la session déconnexion
 */
public class Session {
    private static String jwtToken = null;//Stock le token après co

    public static void setToken(String token) {
        jwtToken = token;
    }

    public static String getToken() {
        return jwtToken;
    }

    public static boolean isConnected() {
        return jwtToken != null;
    }
    private static String username; //Stock nom du joueur

    public static void setUsername(String u) { username = u; }
    public static String getUsername() { return username; }

    /*
     *Session.clear(); = rénitialiser la session (déco du joueur)
     */
    public static void clear() {
        jwtToken = null;
        username = null;
    }
}
