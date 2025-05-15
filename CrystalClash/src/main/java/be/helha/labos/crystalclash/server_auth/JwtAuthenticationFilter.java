package be.helha.labos.crystalclash.server_auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtAuthenticationFilter = permet a SPRING Security de lire le tk JWT depuis les requetes entrante ( le Bearer la )
 * Verif si il est valide, Extraiit le username et role
 *et crée une authentification interne
 * OncePerRequestFilter = execution a chaque requete
 * **/
//Toute les requetes, il vérif le token
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    //injection
    private final JwtUtils jwtUtils;

    /**
     * @param jwtUtils
     * Lis depuis l'en-tete
     * valide la signature et extrait les données du user
     * */
    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * @param request
     * @param response
     * @param chain
     * Recup le token depuis l'entete Authorization (pour ça que dans postman il faut mettre Authorization: Bearer <token>
     *     sinon le user n'est pas autentifier pour faire des requetes
     * **/
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        //Recup le token depuis l'en tete Authorization: Bearer <token>
        String jwtToken = jwtUtils.extractJwtFromRequest(request);

        //Verif que le token existe et si il est valide
        if (jwtToken != null && jwtUtils.validateJwtToken(jwtToken)) {
                String username = jwtUtils.getUsernameFromJwtToken(jwtToken); //Recup username

            //Il faut transformer le role texte du user en objet GrantedAuthority pour que spring comprene le role
            List<GrantedAuthority> authorities = jwtUtils.getRolesFromJwtToken(jwtToken).stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());


            //SI pas null creation d un objet d'authentification sans le mdt juste identifiant et role
            if (username != null) {
                // Créer objet authentication a partir de username, tole
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                //inject dans le SecurityContext l'objet de l'athentif et les details de la requete
                //ip,session
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response); //Continuer la requete
    }
}
