package com.example.vaideboa.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.AlterarSenhaDto;
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
import com.example.vaideboa.validator.CpfValidator;
import com.example.vaideboa.validator.SenhaValidator;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CpfValidator cpfValidator;
    private final SenhaValidator senhaValidator;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, CpfValidator cpfValidator,
            SenhaValidator senhaValidator) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.cpfValidator = cpfValidator;
        this.senhaValidator = senhaValidator;
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
        : "NAO_INFORMADO";
        Preferencias pref = user.getPreferencia();
        PreferenciasDto preferenciasDto = new PreferenciasDto(
            pref != null && pref.getConversa() != null ? pref.getConversa().getDescricao() : "TALVEZ",
            pref != null && pref.getMusica() != null ? pref.getMusica().getDescricao() : "TALVEZ",
            pref != null && pref.getCigarro() != null ? pref.getCigarro().getDescricao() : "TALVEZ",
            pref != null && pref.getAnimais() != null ? pref.getAnimais().getDescricao() : "TALVEZ"
        );
        UserRetornoDto userRetornoDto = new UserRetornoDto(user.getNome(), user.getUsername(), user.getCpf(), user.getTelefone(), dataNascimento, user.getGenero().toString(), preferenciasDto);
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
        boolean alterou = false;

        if(dto.getNome() != null && !dto.getNome().isBlank()) {
            user.setNome(dto.getNome().trim());
            alterou = true;
        }

        if(dto.getTelefone() != null && !dto.getTelefone().isBlank() && dto.getTelefone().matches("\\d{10,11}")) {
            user.setTelefone(dto.getTelefone());
            alterou = true;
        }

        if(dto.getDataNascimento() != null){
            if (dto.getDataNascimento().isAfter(LocalDate.now())) {
                return new ApiResponse(false, "Data de nascimento inválida");
            }
            user.setDataNascimento(dto.getDataNascimento());
            alterou = true;
        }

        if(dto.getGenero() != null){
            user.setGenero(dto.getGenero());
            alterou = true;
        }

        if(dto.getCpf() != null && !dto.getCpf().isBlank()){
            String cpfLimpo = dto.getCpf().replaceAll("[^\\d]", "");

            if(!cpfValidator.cpfValido(cpfLimpo)){
                return new ApiResponse(false,"Cpf digitado é invalido");
            }

            if(!cpfLimpo.equals(user.getCpf())) {
                Optional<User> existente = userRepository.findByCpf(cpfLimpo);

                if(existente.isPresent() && !existente.get().getId().equals(user.getId())){
                    return new ApiResponse(false,"CPF já está em uso");
                }

                user.setCpf(cpfLimpo);
                alterou = true;
            }
        }

        if(!alterou){
            return new ApiResponse(false, "Nenhum dado para atualizar");
        }

        userRepository.save(user);
        return new ApiResponse(true, "Usuário editado com sucesso");
    }

    public ApiResponse alterarSenha(AlterarSenhaDto dto, String username){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User user = userOpt.get();

        if(!passwordEncoder.matches(dto.getSenhaAtual(), user.getPassword())){
            return new ApiResponse(false, "Senha fornecida não bate com a salva");
        }
        if(!SenhaValidator.senhaValida(dto.getNovaSenha())){
            return new ApiResponse(false, "Senha deve conter pelo menos 8 caracteres, letra, número e caractere especial");
        }
        
        if(passwordEncoder.matches(dto.getNovaSenha(), user.getPassword())){
        return new ApiResponse(false, "Nova senha deve ser diferente da atual");
        }

        String novaSenhaCriptografada = passwordEncoder.encode(dto.getNovaSenha());
        user.setPassword(novaSenhaCriptografada);
        userRepository.save(user);
        return new ApiResponse(true, "Senha alterado com sucesso");
    }
}
