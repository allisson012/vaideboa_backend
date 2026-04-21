package com.example.vaideboa.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.EditarUserDto;
import com.example.vaideboa.Dtos.PreferenciasDto;
import com.example.vaideboa.Dtos.UserDto;
import com.example.vaideboa.Dtos.UserRetornoDto;
import com.example.vaideboa.exception.EmailJaCadastradoException;
import com.example.vaideboa.model.Preferencias;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.Generos;
import com.example.vaideboa.model.enums.NivelPreferencia;
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
        user.setGenero(Generos.NAO_INFORMADO);
        // setando as prefencias com valor padrão
        Preferencias preferencias = new Preferencias();
        preferencias.setAnimais(NivelPreferencia.TALVEZ);
        preferencias.setCigarro(NivelPreferencia.TALVEZ);
        preferencias.setConversa(NivelPreferencia.TALVEZ);
        preferencias.setMusica(NivelPreferencia.TALVEZ);
        preferencias.setUser(user);
        user.setPreferencia(preferencias);
        var userRetorno = userRepository.save(user);
        if(userRetorno != null){
            return true;
        }
        return false;
    }

    public UserRetornoDto buscarUserPorUsername(String username){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            throw new RuntimeException("Usuário não encontrado");
        }
        User user = userOpt.get();
        String dataNascimento = user.getDataNascimento() != null
        ? user.getDataNascimento().toString()
        : "Não informado";
        Preferencias pref = user.getPreferencia();
        PreferenciasDto preferenciasDto = new PreferenciasDto(
            pref != null && pref.getConversa() != null ? pref.getConversa().getDescricao() : "TALVEZ",
            pref != null && pref.getMusica() != null ? pref.getMusica().getDescricao() : "TALVEZ",
            pref != null && pref.getCigarro() != null ? pref.getCigarro().getDescricao() : "TALVEZ",
            pref != null && pref.getAnimais() != null ? pref.getAnimais().getDescricao() : "TALVEZ"
        );
        UserRetornoDto userRetornoDto = new UserRetornoDto(user.getNome(), user.getUsername(), user.getCpf(), user.getTelefone(), dataNascimento, user.getGenero().getDescricao(), preferenciasDto);
        return userRetornoDto;
    }

    public boolean excluirUsuario (String username){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return false;
        }
        User user = userOpt.get();
        user.setAtivo(false);
        userRepository.save(user);
        return true;
    }

    public ApiResponse editarUsuario(EditarUserDto dto, String username){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User user = userOpt.get();
        if(dto.getNome() != null && !dto.getNome().isBlank()) {
            user.setNome(dto.getNome());
        }
        if(dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
            user.setTelefone(dto.getTelefone());
        }
        if(dto.getDataNascimento() != null){

        }
        if(dto.getGenero() != null){
            
        }
        return null;
    }
}
