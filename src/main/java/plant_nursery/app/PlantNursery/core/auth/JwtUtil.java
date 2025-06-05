package plant_nursery.app.PlantNursery.core.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {// Ключ
    private static final long EXPIRATION_TIME = 86400000; // 1 день

    public static String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, Constant.JWT_SIGNING_KEY)
                .compact();
    }

    public static String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(Constant.JWT_SIGNING_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    public static String getRoleFromToken(String token) {
        return Jwts.parser().setSigningKey(Constant.JWT_SIGNING_KEY).parseClaimsJws(token).getBody().get("role", String.class);
    }
}
