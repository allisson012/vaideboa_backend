package com.example.vaideboa.security;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;



@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    
    public JwtService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String generateToken(Authentication authentication){
        Instant now =  Instant.now();
        long expiry = 2592000L; // 30 dias

        String scopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        
        var claims = JwtClaimsSet.builder()
        .issuer("vaideboa")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(expiry))
        .subject(authentication.getName())
        .claim("scope",scopes)
        .build();
    
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean tokenValido(String token) {
    try {

        Jwt jwt = decoder.decode(token);

        return jwt.getExpiresAt() != null &&
               jwt.getExpiresAt().isAfter(Instant.now());

    } catch (JwtException e) {
        return false;
    }
    
    }
    public Jwt decode(String token) {
        return decoder.decode(token); 
    }
}
