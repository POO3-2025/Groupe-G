package be.helha.labos.crystalclash.DTO;


/**
 * Classe représentant une requête d'enregistrement.
 * Cette classe est utilisée pour mapper automatiquement le corps JSON reçu
 * lors d'une requête POST sur l'endpoint /register.
 * Exemple de JSON attendu :
 * {
 *   "username": "toto",
 *   "password": "toto" // le mot de passe sera haché en base de données
 * }
 */
public class RegisterRequest {
    private String username;
    private String password;

    /**
     * Constructeur avec paramètres, requis pour les tests.
     * @param username Le nom d'utilisateur.
     * @param password Le mot de passe de l'utilisateur.
     */
    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Récupère le nom d'utilisateur.
     * @return Le nom d'utilisateur.
     */
    public String getUsername() { return username; }

    /**
     * Modifie le nom d'utilisateur.
     * @param username Le nouveau nom d'utilisateur.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Récupère le mot de passe.
     * @return Le mot de passe.
     */
    public String getPassword() { return password; }

    /**
     * Modifie le mot de passe.
     * @param password Le nouveau mot de passe.
     */
    public void setPassword(String password) { this.password = password; }
}