package be.helha.labos.crystalclash.Controller.server_auth;

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
/*
* Permet a spring Security de retrouver l'uti ds la db mysql lors de la connexion
*UserDetailsService interface officielle de springSecurity appelle automatiquement lors d'une tentative de connection
* Dès qu'il y a un login avec un username Spring appelle direct cette méthode pour cherhcer me compte en db
* Connection a la base de données mysqlproduction
*Il y a la requête sql (checher le user avec le username)
* si trouvé on crée un userDetails
*  -> on récup le mdp déjà hashé fans la db et construit l objet User que spring pourra comparer avec le MDP recu depuis /login
* Erreur 401 si pas trouvé
 * */
@Service
public class CustomUserDetailsService implements UserDetailsService {

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
