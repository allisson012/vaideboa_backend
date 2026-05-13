package com.example.vaideboa.security;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{
    private final UserRepository userRepository;
    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException{
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(request);
        String email = oauthUser.getAttribute("email");
        String nome = oauthUser.getAttribute("name");
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(email);

        if(userOpt.isEmpty()){
            User user = new User();
            user.setNome(nome);
            user.setUsername(email);
            user.setPassword("OAUTH_LOGIN");
            userRepository.save(user);
        }

        return oauthUser;
    }
}
