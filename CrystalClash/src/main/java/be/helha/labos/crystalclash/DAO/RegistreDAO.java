package be.helha.labos.crystalclash.DAO;

/**
 * Interface définissant les opérations liées à l'enregistrement des utilisateurs
 * dans la base de données, comme la vérification d'existence et l'insertion.
 */
public interface RegistreDAO {

    /**
     * Vérifie si un utilisateur existe déjà dans la base de données.
     *
     * @param username le nom d'utilisateur à vérifier
     * @return true si l'utilisateur existe,sinon false
     * @throws Exception en cas d'erreur lors de l'accès à la base de données
     */
    boolean userExists(String username) throws Exception;

    /**
     * Insère un nouvel utilisateur avec un mot de passe chiffré dans la base de données.
     *
     * @param username le nom d'utilisateur à insérer
     * @param hashedPassword le mot de passe déjà chiffré (hashé)
     * @throws Exception en cas d'erreur lors de l'insertion dans la base de données
     */
    void insertUser(String username, String hashedPassword) throws Exception;
}