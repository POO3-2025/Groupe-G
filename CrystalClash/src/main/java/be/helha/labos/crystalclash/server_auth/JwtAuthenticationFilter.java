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
 * Sécu pour le token JWT
 * sert a lire le token envoyé par le cliant a chaque requete $
 * Verifier si il est valide
 * Extration du nom de l'uti et role du token
 * et apres ok joueur bien authentifé
 * **/
//Toute les requetes, il vérif le token
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    //lire, valider et decoder le token recu
    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * @param request
     * @param response
     * @param chain
     * **/
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        //Recup le token depuis l'en tete Authorization: Bearer <token>
        String jwtToken = jwtUtils.extractJwtFromRequest(request);

        //Si token est bien la et valide avec la signature
        if (jwtToken != null && jwtUtils.validateJwtToken(jwtToken)) {
                String username = jwtUtils.getUsernameFromJwtToken(jwtToken); //Recup username

            //Il faut transformer le role texte du user en GrantedAuthority pour que spring comprene le role
            List<GrantedAuthority> authorities = jwtUtils.getRolesFromJwtToken(jwtToken).stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());


            //SI pas null creation d un objet d'authentification sans le mdt juste identifiant et role
            if (username != null) {
                // Créer une authentification basée sur le token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                //ajoute des détailes sur la requete, la session, l'ip,ect,....
                //et dit ok uti identifié
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}
