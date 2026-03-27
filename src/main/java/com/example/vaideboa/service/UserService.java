package com.example.vaideboa.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.UserDto;
import com.example.vaideboa.exception.EmailJaCadastradoException;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public boolean cadastrarUser(UserDto userDto){
        if(userRepository.existsByUsername(userDto.getUsername())){
        throw new EmailJaCadastradoException();
    }

        String senhaCriptografada = passwordEncoder.encode(userDto.getPassword());
        User user = new User();
        user.setNome(userDto.getNome());
        user.setUsername(userDto.getUsername());
        user.setPassword(senhaCriptografada);
        user.setAtivo(true);
        user.setContaNaoBloqueada(true);
        user.setContaNaoExpirada(true);
        user.setCredenciaisNaoExpiradas(true);
        user.setGenero("Masculino");
        var userRetorno = userRepository.save(user);
        if(userRetorno != null){
            return true;
        }
        return false;
    }
}
