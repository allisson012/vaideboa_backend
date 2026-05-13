package com.example.vaideboa.service;
import com.example.vaideboa.Dtos.CaronaDto;
import com.example.vaideboa.Dtos.RotaInfoDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Rota;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.RotaRepository;
import com.example.vaideboa.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class CaronaServiceTest {

//  TC-01 a TC-07 — Testes unitários de CaronaService.cadastrarCarona()

//  Técnicas aplicadas:
//    • Partição de equivalência  (usuário existe / não existe)
//    • Análise de valor limite    (coordenadas extremas, assentos = 0 e 1)
//    • Tabela de decisão          (combinações de falhas de dependências)
//    • Caixa-cinza               (verificação de fluxo interno via mocks)

//  Ferramentas: JUnit 5 + Mockito

    @Mock private UserRepository    userRepository;
    @Mock private RotaRepository    rotaRepository;
    @Mock private CaronaRepository  caronaRepository;
    @Mock private RotaService       rotaService;
    @Mock private GeoService        geoService;

    @InjectMocks
    private CaronaService caronaService;

    // ── Objetos reutilizados entre testes ────────────────────
    private GeometryFactory geometryFactory;
    private CaronaDto       caronaDtoValido;
    private User            usuarioAtivo;
    private Rota            rotaSalva;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // DTO com dados válidos (origem: Campinas, destino: São Paulo) ordem qntAssentos,  data,  hora,  saidaLat,  saidaLng,
            // destinoLat,  destinoLng
        caronaDtoValido = new CaronaDto(3,LocalDate.of(2025, 6, 10),LocalTime.of(8, 0),-22.9064,-47.0626,-23.5505,-46.6333);

        // Usuário ativo padrão
        usuarioAtivo = new User();
        usuarioAtivo.setUsername("joao123");

        // Rota persistida simulada
        rotaSalva = new Rota();
        rotaSalva.setId(1L);
    }

    // ────────────────────────────────────────────────────────
    // TC-01 — Caminho feliz: cadastro bem-sucedido
    // Técnica: Partição de equivalência (classe válida)
    // ────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-01 | Deve retornar true quando todos os dados são válidos")
    void deveRetornarTrueQuandoCadastroEhValido() {
        // Arrange
        when(userRepository.findByUsernameAndAtivoTrue("joao123"))
                .thenReturn(Optional.of(usuarioAtivo));

        String geojsonMock = "{\"type\":\"FeatureCollection\"}";
        LineString trajetoMock = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(-47.0626, -22.9064),
                new Coordinate(-46.6333, -23.5505)
        });
        RotaInfoDto rotaInfo = new RotaInfoDto(130.5, 85.0);

        when(rotaService.getRota(any(Point.class), any(Point.class))).thenReturn(geojsonMock);
        when(rotaService.salvarRota(geojsonMock)).thenReturn(trajetoMock);
        when(rotaService.extrairInfoRota(geojsonMock)).thenReturn(rotaInfo);
        when(geoService.reverseGeocode(anyDouble(), anyDouble())).thenReturn("Endereço Mock");
        when(rotaRepository.save(any(Rota.class))).thenReturn(rotaSalva);
        when(caronaRepository.save(any(Carona.class))).thenReturn(new Carona());

        // Act
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        // Assert
        assertTrue(resultado, "Esperava true para cadastro válido");
        verify(rotaRepository, times(1)).save(any(Rota.class));
        verify(caronaRepository, times(1)).save(any(Carona.class));
    }

    // ────────────────────────────────────────────────────────
    // TC-02 — Usuário não encontrado
    // Técnica: Partição de equivalência (classe inválida)
    // ────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-02 | Deve retornar false quando o username não existe ou está inativo")
    void deveRetornarFalseQuandoUsuarioNaoExiste() {
        // Arrange
        when(userRepository.findByUsernameAndAtivoTrue("fantasma"))
                .thenReturn(Optional.empty());

        // Act
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "fantasma");

        // Assert
        assertFalse(resultado, "Esperava false para usuário inexistente");
        // Nenhuma outra dependência deve ser chamada
        verifyNoInteractions(rotaService, rotaRepository, caronaRepository, geoService);
    }

    // ────────────────────────────────────────────────────────
    // TC-03 — Falha ao salvar a Rota (rotaRepository retorna null)
    // Técnica: Tabela de decisão (falha na camada de persistência da rota)
    // ────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-03 | Deve retornar false quando rotaRepository.save() retorna null")
    void deveRetornarFalseQuandoRotaNaoESalva() {
        // Arrange
        when(userRepository.findByUsernameAndAtivoTrue("joao123"))
                .thenReturn(Optional.of(usuarioAtivo));

        String geojsonMock = "{\"type\":\"FeatureCollection\"}";
        LineString trajetoMock = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(-47.0626, -22.9064),
                new Coordinate(-46.6333, -23.5505)
        });
        RotaInfoDto rotaInfo = new RotaInfoDto(130.5, 85.0);

        when(rotaService.getRota(any(Point.class), any(Point.class))).thenReturn(geojsonMock);
        when(rotaService.salvarRota(geojsonMock)).thenReturn(trajetoMock);
        when(rotaService.extrairInfoRota(geojsonMock)).thenReturn(rotaInfo);
        when(geoService.reverseGeocode(anyDouble(), anyDouble())).thenReturn("Endereço Mock");
        when(rotaRepository.save(any(Rota.class))).thenReturn(null); // ← falha simulada

        // Act
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        // Assert
        assertFalse(resultado, "Esperava false quando a rota não é salva");
        verify(caronaRepository, never()).save(any(Carona.class)); // carona NÃO deve ser salva
    }

    // ────────────────────────────────────────────────────────
    // TC-04 — Limite mínimo de assentos: 1 assento
    // Técnica: Análise de valor limite (fronteira inferior)
    // ────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-04 | Deve aceitar carona com apenas 1 assento (valor limite mínimo)")
    void deveAceitarUmAssentoComoLimiteMinimo() {
        // Arrange
        caronaDtoValido = new CaronaDto(1,LocalDate.of(2025, 6, 10),LocalTime.of(8, 0),-22.9064,-47.0626,-23.5505,-46.6333);

        when(userRepository.findByUsernameAndAtivoTrue("joao123"))
                .thenReturn(Optional.of(usuarioAtivo));

        String geojsonMock = "{\"type\":\"FeatureCollection\"}";
        LineString trajetoMock = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(-47.0626, -22.9064),
                new Coordinate(-46.6333, -23.5505)
        });

        when(rotaService.getRota(any(), any())).thenReturn(geojsonMock);
        when(rotaService.salvarRota(any())).thenReturn(trajetoMock);
        when(rotaService.extrairInfoRota(any())).thenReturn(new RotaInfoDto(130.5, 85.0));
        when(geoService.reverseGeocode(anyDouble(), anyDouble())).thenReturn("Endereço");
        when(rotaRepository.save(any())).thenReturn(rotaSalva);
        when(caronaRepository.save(any())).thenAnswer(inv -> {
            Carona c = inv.getArgument(0);
            // Valida que vagasDisponiveis == qntAssentos == 1
            assertEquals(1, c.getQntAssentos());
            assertEquals(1, c.getVagasDisponiveis());
            return c;
        });

        // Act
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        // Assert
        assertTrue(resultado);
    }

    // ────────────────────────────────────────────────────────
    // TC-05 — Coordenadas extremas (valores-limite geográficos)
    // Técnica: Análise de valor limite (coordenadas máximas válidas)
    // ────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-05 | Deve processar coordenadas nos limites geográficos máximos")
    void deveProcessarCoordenadasExtremасValidas() {
        
        caronaDtoValido = new CaronaDto(3,LocalDate.of(2025, 6, 10),LocalTime.of(8, 0),90.0,180.0,-90.0,-180.0);

        when(userRepository.findByUsernameAndAtivoTrue("joao123"))
                .thenReturn(Optional.of(usuarioAtivo));

        String geojsonMock = "{\"type\":\"FeatureCollection\"}";
        LineString trajetoMock = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(180.0, 90.0),
                new Coordinate(-180.0, -90.0)
        });

        when(rotaService.getRota(any(), any())).thenReturn(geojsonMock);
        when(rotaService.salvarRota(any())).thenReturn(trajetoMock);
        when(rotaService.extrairInfoRota(any())).thenReturn(new RotaInfoDto(20015.0, 9999.0));
        when(geoService.reverseGeocode(anyDouble(), anyDouble())).thenReturn("Extremo");
        when(rotaRepository.save(any())).thenReturn(rotaSalva);
        when(caronaRepository.save(any())).thenReturn(new Carona());

        // Act
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        // Assert
        assertTrue(resultado, "Coordenadas limítrofes válidas devem ser aceitas");
    }

    // ────────────────────────────────────────────────────────
    // TC-06 — Motorista correto associado à carona
    // Técnica: Caixa-cinza (inspeção do estado interno do objeto salvo)
    // ────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-06 | Deve associar o motorista correto à carona cadastrada")
    void deveAssociarMotoristaCorretoNaCarona() {
        // Arrange
        when(userRepository.findByUsernameAndAtivoTrue("joao123"))
                .thenReturn(Optional.of(usuarioAtivo));

        String geojsonMock = "{\"type\":\"FeatureCollection\"}";
        LineString trajetoMock = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(-47.0, -22.9),
                new Coordinate(-46.6, -23.5)
        });

        when(rotaService.getRota(any(), any())).thenReturn(geojsonMock);
        when(rotaService.salvarRota(any())).thenReturn(trajetoMock);
        when(rotaService.extrairInfoRota(any())).thenReturn(new RotaInfoDto(130.5, 85.0));
        when(geoService.reverseGeocode(anyDouble(), anyDouble())).thenReturn("Endereço");
        when(rotaRepository.save(any())).thenReturn(rotaSalva);

        // Captura o objeto Carona que seria salvo
        when(caronaRepository.save(any(Carona.class))).thenAnswer(invocation -> {
            Carona caronaSalva = invocation.getArgument(0);
            // Assert interno: verifica se o motorista foi atribuído corretamente
            assertNotNull(caronaSalva.getMotorista());
            assertEquals("joao123", caronaSalva.getMotorista().getUsername());
            assertEquals(rotaSalva, caronaSalva.getRota());
            return caronaSalva;
        });

        // Act
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        // Assert
        assertTrue(resultado);
    }

    // ────────────────────────────────────────────────────────
    // TC-07 — Exceção lançada pelo serviço de rota externo
    // Técnica: Tabela de decisão (falha em serviço externo)
    // ────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-07 | Deve propagar RuntimeException quando RotaService falha")
    void devePropagарExcecaoQuandoRotaServiceFalha() {
        // Arrange
        when(userRepository.findByUsernameAndAtivoTrue("joao123"))
                .thenReturn(Optional.of(usuarioAtivo));
        when(rotaService.getRota(any(Point.class), any(Point.class)))
                .thenThrow(new RuntimeException("API de rota indisponível"));

        // Act + Assert
        assertThrows(RuntimeException.class,
                () -> caronaService.cadastrarCarona(caronaDtoValido, "joao123"),
                "Esperava RuntimeException quando RotaService lança exceção"
        );

        // Nenhuma persistência deve ocorrer
        verifyNoInteractions(rotaRepository, caronaRepository);
    }
 
}
