package com.example.vaideboa.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.AlterarSenhaDto;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.EditarUserDto;
import com.example.vaideboa.Dtos.PreferenciasDto;
import com.example.vaideboa.Dtos.PerfilPublicoDto;
import com.example.vaideboa.Dtos.RankingDto;
import com.example.vaideboa.Dtos.UserDto;
import com.example.vaideboa.Dtos.UserRetornoDto;
import com.example.vaideboa.exception.EmailJaCadastradoException;
import com.example.vaideboa.model.Preferencias;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.Generos;
import com.example.vaideboa.model.enums.NivelPreferencia;
import com.example.vaideboa.repository.UserRepository;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.PedidoCaronaRepository;
import com.example.vaideboa.validator.CpfValidator;
import com.example.vaideboa.validator.SenhaValidator;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CpfValidator cpfValidator;
    private final SenhaValidator senhaValidator;
    private final AvaliacaoService avaliacaoService;
    private final CaronaRepository caronaRepository;
    private final PedidoCaronaRepository pedidoCaronaRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, CpfValidator cpfValidator,
            SenhaValidator senhaValidator, AvaliacaoService avaliacaoService, CaronaRepository caronaRepository,
            PedidoCaronaRepository pedidoCaronaRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.cpfValidator = cpfValidator;
        this.senhaValidator = senhaValidator;
        this.avaliacaoService = avaliacaoService;
        this.caronaRepository = caronaRepository;
        this.pedidoCaronaRepository = pedidoCaronaRepository;
    }

    public boolean cadastrarUser(UserDto userDto){
        if(userRepository.existsByUsername(userDto.getUsername())){
        throw new EmailJaCadastradoException();
        }

        String senhaCriptografada = passwordEncoder.encode(userDto.getPassword());
        User user = new User();
        user.setNome(userDto.getNome());
        user.setUsername(userDto.getUsername());
        // validar se a senha corresponde a estrutura definida letra maiscula etc
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
            pref != null && pref.getConversa() != null ? pref.getConversa(): NivelPreferencia.TALVEZ,
            pref != null && pref.getMusica() != null ? pref.getMusica(): NivelPreferencia.TALVEZ,
            pref != null && pref.getCigarro() != null ? pref.getCigarro(): NivelPreferencia.TALVEZ,
            pref != null && pref.getAnimais() != null ? pref.getAnimais(): NivelPreferencia.TALVEZ
        );

        RankingDto rankingDto = new RankingDto();
        rankingDto = avaliacaoService.calculaRanking(user);
        UserRetornoDto userRetornoDto = new UserRetornoDto(
            user.getNome(), 
            user.getUsername(),
            user.getCpf(),
            user.getTelefone(), 
            dataNascimento,
            user.getGenero().toString(), 
            preferenciasDto,
            rankingDto
        );
        return userRetornoDto;
    }

    public ApiResponse buscarPerfilPublico(Long idUsuario, Long idCarona, String username) {
        User solicitante = userRepository.findByUsernameAndAtivoTrue(username).orElse(null);
        User perfil = userRepository.findById(idUsuario).filter(User::isAtivo).orElse(null);
        Carona carona = caronaRepository.findById(idCarona).orElse(null);
        if (solicitante == null || perfil == null || carona == null) {
            return new ApiResponse(false, "Perfil não encontrado");
        }

        boolean perfilDoMotorista = carona.getMotorista().getId().equals(perfil.getId());
        boolean solicitanteEMotorista = carona.getMotorista().getId().equals(solicitante.getId());
        boolean solicitanteEPerfil = solicitante.getId().equals(perfil.getId());
        boolean passageiroDaCarona = pedidoCaronaRepository.existsByPassageiroAndCarona(perfil, carona);
        if (!perfilDoMotorista && !solicitanteEPerfil && !(solicitanteEMotorista && passageiroDaCarona)) {
            return new ApiResponse(false, "Usuário não tem acesso a este perfil");
        }

        Preferencias preferencias = perfil.getPreferencia();
        PreferenciasDto preferenciasDto = new PreferenciasDto(
            preferencias != null && preferencias.getConversa() != null ? preferencias.getConversa() : NivelPreferencia.TALVEZ,
            preferencias != null && preferencias.getMusica() != null ? preferencias.getMusica() : NivelPreferencia.TALVEZ,
            preferencias != null && preferencias.getCigarro() != null ? preferencias.getCigarro() : NivelPreferencia.TALVEZ,
            preferencias != null && preferencias.getAnimais() != null ? preferencias.getAnimais() : NivelPreferencia.TALVEZ
        );
        PerfilPublicoDto perfilDto = new PerfilPublicoDto(
            perfil.getId(), perfil.getNome(), perfil.getFoto(), perfil.getGenero().getDescricao(),
            preferenciasDto, avaliacaoService.calculaRanking(perfil)
        );
        return new ApiResponse(true, "Perfil encontrado com sucesso", perfilDto);
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

    public ApiResponse atualizarPreferencias(PreferenciasDto dto, String username){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);

        if (userOpt.isEmpty()){
            return new ApiResponse (false, "Usuário não encontrado", null);
        }
        User user = userOpt.get();
        Preferencias pref = user.getPreferencia();

        if (pref==null){
            pref = new Preferencias();
        }
        try{
            pref.setConversa(dto.getConversa());
            pref.setMusica(dto.getMusica());
            pref.setCigarro(dto.getCigarro());
            pref.setAnimais(dto.getAnimais());
        } catch (IllegalArgumentException e){
            return new ApiResponse(false, "Valor inválido para preferências", null);
        }
        

        user.setPreferencia(pref);
        userRepository.save(user);
        return new ApiResponse(true, "Preferências atualizadas com sucesso", pref);
    }

}
