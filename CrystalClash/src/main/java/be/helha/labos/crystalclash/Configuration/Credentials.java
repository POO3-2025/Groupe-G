package be.helha.labos.crystalclash.Configuration;

/**
 * Classe représentant les informations d'identification pour accéder à une base de données.
 *
 * Cette classe contient les détails nécessaires pour établir une connexion à une base de données,
 * y compris le nom d'hôte, le nom d'utilisateur, le mot de passe, le nom de la base de données et le port.
 */
public class Credentials {
    /**
     * Constructeur par défaut.
     */
    public Credentials() {
        // Constructeur vide
    }
    /** Le nom d'hôte du serveur de base de données (par exemple, "localhost" ou une adresse IP). */
    public String HostName;

    /** Le nom d'utilisateur pour se connecter à la base de données. */
    public String UserName;

    /** Le mot de passe pour se connecter à la base de données. */
    public String Password;

    /** Le nom de la base de données à laquelle se connecter. */
    public String DBName;

    /** Le port utilisé pour la connexion à la base de données (par exemple, 3306 pour MySQL ou 1433 pour SQL Server). */
    public int port;
}
