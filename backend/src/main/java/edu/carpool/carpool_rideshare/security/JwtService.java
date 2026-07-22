package edu.carpool.carpool_rideshare.security;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;


@Component
public class JwtService {
    
    @Value("${app.jwt.secret}")
    private String securityKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(securityKey.getBytes());
    }
    
    public String generateToken(String email){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String email){
        String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpiered(token);
    }

    public boolean isTokenExpiered(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

}
