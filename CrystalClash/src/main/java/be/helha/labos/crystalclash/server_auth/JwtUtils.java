package be.helha.labos.crystalclash.server_auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * C'est le cerveau de la secu JWT
 * Permet la creation, lecture validation, extraction du username et role sur le token
 * **/
@Component
public class JwtUtils {
    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final int jwtExpirationMs = 3600000;   // DurÃ©e en millisecondes (1 heure)

    /**
     * @param authentication
     * Recup l'uti avec authentication.getPrincipal();
     * prend son username et recup ses roles
     * et lui genere un JWT token signé
     *
     **/
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
            .setSubject(userDetails.getUsername())// identifiant du joueur
            .claim("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)//  ROLE_USER
                .collect(Collectors.joining(",")))// "ROLE_USER"
            .setIssuedAt(new Date())// date d’émission
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))// date d’expiration
            .signWith(key)// signature du token avec la clé secrète
            .compact();// construit le token
    }

    /**
     * @param request
     * Extraction du Jwt depuis une requete
     * Lit L'en tete http supp le Bearer
     * */
    public String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    /**
     * @param token
     * Extrait username et dechiffrant le token et en lissant getSubject qui contient le username
     * **/
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    /**
     * Verif si token ok
     * bien signé, pas expiré et bien formé
     * **/
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param token
     * extrait role du token
     * claim stocke role et fais une liste
     * **/
    public List<String> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        return Arrays.asList(claims.get("roles").toString());
    }

}
