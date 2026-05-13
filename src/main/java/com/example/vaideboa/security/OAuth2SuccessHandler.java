package com.example.vaideboa.security;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    public OAuth2SuccessHandler(JwtEncoder jwtEncoder, UserRepository userRepository){
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByUsernameAndAtivoTrue(email).orElseThrow();
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("vaideboa")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(86400))
                .claim("id", user.getId())
                .claim("nome", user.getNome())
                .build();
        
        String token = jwtEncoder.encode(
            JwtEncoderParameters.from(claims)
        ).getTokenValue();
        response.setContentType("application/json");
        response.getWriter().write(
            """
            {
                "token": "%s"
            }        
            """.formatted(token)
        );
    }
}
