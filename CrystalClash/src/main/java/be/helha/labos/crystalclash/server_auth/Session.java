package be.helha.labos.crystalclash.Controller.server_auth;

import be.helha.labos.crystalclash.User.UserInfo;


/*
 *Stocker en local les infos du joueur connecté pendant que le jeu tourne

garde le token jwt que le serv sping envoie apres le login
garde le nom du joueur
permet de vérifier si 1 joueur est connecté ou non
et la session déconnexion*/
public class Session {
    private static String jwtToken = null;//Stock le token après co

    private static UserInfo userInfo;

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
    public static void setUserInfo(UserInfo info) {
        userInfo = info;
    }

    public static UserInfo getUserInfo() {
        return userInfo;
    }
    /*
     *Session.clear(); = rénitialiser la session (déco du joueur)
     */
    public static void clear() {
        jwtToken = null;
        username = null;
        userInfo = null;
    }
}
