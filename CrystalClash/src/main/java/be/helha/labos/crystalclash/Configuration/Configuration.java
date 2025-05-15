package be.helha.labos.crystalclash.Configuration;

/**
 * Classe représentant la configuration de la base de données.
 *
 * Cette classe contient les informations nécessaires pour établir une connexion à la base de données,
 * y compris le type de connexion, le type de base de données et les informations d'identification.
 */
public class Configuration {
    /**
     * Constructeur par défaut.
     */
    public Configuration() {
        // Constructeur vide
    }
    /** Le type de connexion à utiliser (par exemple, "local" ou "remote"). */
    public String ConnectionType;

    /** Le type de base de données (par exemple, "sqlserver", "mysql", etc.). */
    public String DBType;

    /** Les informations d'identification pour accéder à la base de données. */
    public Credentials BDCredentials;
}
