package be.helha.labos.crystalclash.server_auth;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;

/**
 *UserDetailsService = authentifier les uti depuis la base de données
 * Classe qui permet a Spring Sercurity de recup un uti ds la db lors de la connexion /login
 * @service = detectable auto par spring
 * UserDetailsService = interface de spring use pour charger les infos d'un uti en base depuis son username
 * **/
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * @param username
     * méthode call automatiquement psa spring lors d'1 tentative de co /lohin
     * recoit un username du form en JSON et doit renvoyer objet userDetails qui contient le username et le mdp deja hashé lui
     * et le role
     * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) { // Utilisation du singleton
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String password = rs.getString("password");

                return User.builder()
                        .username(username)
                        .password(password)
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                        .build();
            } else {
                throw new UsernameNotFoundException("Utilisateur non trouvé : " + username);
            }

        } catch (Exception e) {
            throw new UsernameNotFoundException("Erreur lors de la recherche de l'utilisateur : " + e.getMessage());
        }
    }
}
