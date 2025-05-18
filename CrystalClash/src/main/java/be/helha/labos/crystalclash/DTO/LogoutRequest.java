package be.helha.labos.crystalclash.DTO;


/**
 * Classe représentant une requête de déconnexion.
 * Cette classe est utilisée pour transmettre les informations nécessaires
 * à la déconnexion d'un utilisateur, notamment son nom d'utilisateur.
 */
public class LogoutRequest {
    private String username;

    /**
     * Constructeur par défaut de la classe LogoutRequest.
     */
    public LogoutRequest() { }

    /**
     * Constructeur avec paramètre pour initialiser le nom d'utilisateur.
     * @param username Le nom d'utilisateur de l'utilisateur à déconnecter.
     */
    public LogoutRequest(String username) {
        this.username = username;
    }

    /**
     * Récupère le nom d'utilisateur.
     * @return Le nom d'utilisateur.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Modifie le nom d'utilisateur.
     * @param username Le nouveau nom d'utilisateur.
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
