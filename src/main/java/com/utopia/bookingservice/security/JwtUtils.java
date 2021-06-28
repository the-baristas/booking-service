package com.utopia.bookingservice.security;

import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import com.google.common.net.HttpHeaders;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtils {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final Integer EXPIRATION_AFTER_DAYS = 14;
    public static final String HEADER_STRING = "Authorization";

    public static String getTokenPrefix() {
        return TOKEN_PREFIX;
    }

    public static Integer getTokenExpirationAfterDays() {
        return EXPIRATION_AFTER_DAYS;
    }

    public static String getAuthorizationHeader() {
        return HttpHeaders.AUTHORIZATION;
    }

    public static SecretKey getSecretKey(String rawSecretKey) {
        return Keys.hmacShaKeyFor(rawSecretKey.getBytes());
    }

    public static String getUsernameFromToken(String bearerToken,
            String secretKey) {
        Jws<Claims> claims = getJwsClaims(bearerToken, secretKey);

        // Retrieve subject (username)
        return claims.getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public static String getRoleFromToken(String bearerToken,
            String secretKey) {
        Jws<Claims> claims = getJwsClaims(bearerToken, secretKey);
        List<Map<String, String>> authorities = (List<Map<String, String>>) claims
                .getBody().get("authorities");

        return authorities.get(0).get("authority");
    }

    public static Jws<Claims> getJwsClaims(String bearerToken,
            String secretKey) {
        // Retrieve the token from the header, and remove the "Bearer: " portion
        // from the token
        String token = bearerToken.replace(JwtUtils.TOKEN_PREFIX, "");

        // Claims represents the fields of a JWT (sub, authorities, etc) as an
        // object
        return Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build()
                .parseClaimsJws(token);
    }
}
