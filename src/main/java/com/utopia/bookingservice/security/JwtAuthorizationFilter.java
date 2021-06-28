package com.utopia.bookingservice.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private String secretKey;

    public JwtAuthorizationFilter(AuthenticationManager authManager,
            String secretKey) {
        super(authManager);
        this.secretKey = secretKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(JwtUtils.HEADER_STRING);

        if (Strings.isNullOrEmpty(token)
                || !token.startsWith(JwtUtils.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Claims represents the fields of a JWT (sub, authorities, etc) as
            // an object
            Jws<Claims> claims = JwtUtils.getJwsClaims(token, secretKey);

            // Retrieve the necessary fields
            String username = claims.getBody().getSubject();
            List<Map<String, String>> authorities = (List<Map<String, String>>) claims
                    .getBody().get("authorities");

            // Build the simpleGrantedAuthorities
            Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities
                    .stream()
                    .map(m -> new SimpleGrantedAuthority(m.get("authority")))
                    .collect(Collectors.toSet());

            // Set the security context for the current session to reflect this
            // token
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, simpleGrantedAuthorities);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Invalid or expired token.");

        }
        filterChain.doFilter(request, response);
    }
}
