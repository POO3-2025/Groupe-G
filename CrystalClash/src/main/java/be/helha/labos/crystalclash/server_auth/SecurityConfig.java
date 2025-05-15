package be.helha.labos.crystalclash.server_auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Securité backend
 * quelles routes ouvertes ou fermées
 * comment géres l'thentification
 * et quels filtres a appliquer
 * **/
@Configuration //Classe de config spring
@EnableWebSecurity //Active le systeme de secu pring
@EnableMethodSecurity(securedEnabled = true) //Permet l'uti de certaines annotations
public class SecurityConfig  {
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * @param http
     * methode configure le comportement secu global de l'application
     * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactiver CSRF pour les tests (facultatif pour les APIs REST)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register","/inventory,/users/**").permitAll()
                        .anyRequest().authenticated()
                )
            //Tout repose sur le token JWT
                .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            //chaque requete passe par ici
            // lire le jwt, verdif sa validité et authentifie le user
            .addFilterBefore(
                       new JwtAuthenticationFilter(jwtUtils),
                        UsernamePasswordAuthenticationFilter.class)
                ;




        return http.build();
    }

    // bean permet à Spring de gérer l’authentification classique
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    //chiffre mdp
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

