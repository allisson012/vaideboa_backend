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


    @Mock private UserRepository    userRepository;
    @Mock private RotaRepository    rotaRepository;
    @Mock private CaronaRepository  caronaRepository;
    @Mock private RotaService       rotaService;
    @Mock private GeoService        geoService;

    @InjectMocks
    private CaronaService caronaService;

    private GeometryFactory geometryFactory;
    private CaronaDto       caronaDtoValido;
    private User            usuarioAtivo;
    private Rota            rotaSalva;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        caronaDtoValido = new CaronaDto(3,LocalDate.of(2025, 6, 10),LocalTime.of(8, 0),-22.9064,-47.0626,-23.5505,-46.6333);

        usuarioAtivo = new User();
        usuarioAtivo.setUsername("joao123");


        rotaSalva = new Rota();
        rotaSalva.setId(1L);
    }

 
  
    @Test
    @DisplayName("TC-01 | Deve retornar true quando todos os dados são válidos")
    void deveRetornarTrueQuandoCadastroEhValido() {
      
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

        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        
        assertTrue(resultado, "Esperava true para cadastro válido");
        verify(rotaRepository, times(1)).save(any(Rota.class));
        verify(caronaRepository, times(1)).save(any(Carona.class));
    }

   
    @Test
    @DisplayName("TC-02 | Deve retornar false quando o username não existe ou está inativo")
    void deveRetornarFalseQuandoUsuarioNaoExiste() {
      
        when(userRepository.findByUsernameAndAtivoTrue("fantasma"))
                .thenReturn(Optional.empty());

     
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "fantasma");

        assertFalse(resultado, "Esperava false para usuário inexistente");
      
        verifyNoInteractions(rotaService, rotaRepository, caronaRepository, geoService);
    }

    @Test
    @DisplayName("TC-03 | Deve retornar false quando rotaRepository.save() retorna null")
    void deveRetornarFalseQuandoRotaNaoESalva() {
       
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

        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        assertFalse(resultado, "Esperava false quando a rota não é salva");
        verify(caronaRepository, never()).save(any(Carona.class)); // carona NÃO deve ser salva
    }

    @Test
    @DisplayName("TC-04 | Deve aceitar carona com apenas 1 assento (valor limite mínimo)")
    void deveAceitarUmAssentoComoLimiteMinimo() {
       
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

        assertEquals(1, c.getQntAssentos());
        assertEquals(1, c.getVagasDisponiveis());

        return c;
        });

      
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

        
        assertTrue(resultado);
    }

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

   
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

   
        assertTrue(resultado, "Coordenadas limítrofes válidas devem ser aceitas");
    }

 
    @Test
    @DisplayName("TC-06 | Deve associar o motorista correto à carona cadastrada")
    void deveAssociarMotoristaCorretoNaCarona() {
      
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

        
        when(caronaRepository.save(any(Carona.class))).thenAnswer(invocation -> {
            Carona caronaSalva = invocation.getArgument(0);
            
            assertNotNull(caronaSalva.getMotorista());
            assertEquals("joao123", caronaSalva.getMotorista().getUsername());
            assertEquals(rotaSalva, caronaSalva.getRota());
            return caronaSalva;
        });

      
        boolean resultado = caronaService.cadastrarCarona(caronaDtoValido, "joao123");

     
        assertTrue(resultado);
    }


    @Test
    @DisplayName("TC-07 | Deve propagar RuntimeException quando RotaService falha")
    void devePropagарExcecaoQuandoRotaServiceFalha() {
       
        when(userRepository.findByUsernameAndAtivoTrue("joao123"))
                .thenReturn(Optional.of(usuarioAtivo));
        when(rotaService.getRota(any(Point.class), any(Point.class)))
                .thenThrow(new RuntimeException("API de rota indisponível"));

       
        assertThrows(RuntimeException.class,
                () -> caronaService.cadastrarCarona(caronaDtoValido, "joao123"),
                "Esperava RuntimeException quando RotaService lança exceção"
        );

        
        verifyNoInteractions(rotaRepository, caronaRepository);
    }
 
}
