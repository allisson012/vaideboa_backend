package com.example.vaideboa.Integration;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CaronaDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.ReservaCodigo;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusCarona;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.ReservaCodigoRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.repository.UserRepository;
import com.example.vaideboa.service.CaronaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CaronaIntegrationTest {


    @Autowired
    private CaronaService caronaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaronaRepository caronaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaCodigoRepository reservaCodigoRepository;

    @Test
    void deveCadastrarCaronaRealmenteNoBanco() {

        // Arrange
        User user = new User();
        user.setUsername("joao123");
        user.setAtivo(true);

        userRepository.save(user);

        CaronaDto dto = new CaronaDto(
                3,
                LocalDate.now().plusDays(1),
                LocalTime.of(8, 0),
                -22.9064,
                -47.0626,
                -23.5505,
                -46.6333
        );

        // Act
        boolean resultado =
                caronaService.cadastrarCarona(dto, "joao123");

        // Assert
        assertTrue(resultado);

        List<Carona> caronas =
                caronaRepository.findAll();

        assertFalse(caronas.isEmpty());

        Carona caronaSalva = caronas.get(0);

        assertEquals(3, caronaSalva.getQntAssentos());

        assertEquals(
                "joao123",
                caronaSalva.getMotorista().getUsername()
        );

        assertNotNull(caronaSalva.getRota());

        assertNotNull(caronaSalva.getRota().getTrajeto());

        assertNotNull(caronaSalva.getRota().getDistancia());

        assertNotNull(caronaSalva.getRota().getDuracao());
    }

        @Test
    void deveIniciarCaronaEGerarCodigosUnicos() {

        User motorista = new User();
        motorista.setUsername("motorista");
        motorista = userRepository.save(motorista);

        User p1 = new User();
        p1.setAtivo(true);
        p1.setUsername("p1@gmail.com");
        p1.setPassword("123456");
        userRepository.save(p1);
        User p2 = new User();
        p2.setAtivo(true);
        p2.setUsername("p2@gmail.com");
        p2.setPassword("123456");
        userRepository.save(p2);
        User p3 = new User();
        p3.setAtivo(true);
        p3.setUsername("p3@gmail.com");
        p3.setPassword("123456");
        userRepository.save(p3);

        Carona carona = new Carona();
        carona.setMotorista(motorista);
        carona.setData(LocalDate.now());

        carona = caronaRepository.save(carona);

        Reserva r1 = new Reserva();
        r1.setPassageiro(p1);
        r1.setCarona(carona);

        Reserva r2 = new Reserva();
        r2.setPassageiro(p2);
        r2.setCarona(carona);

        Reserva r3 = new Reserva();
        r3.setPassageiro(p3);
        r3.setCarona(carona);

        reservaRepository.saveAll(List.of(r1, r2, r3));

        carona.setReservas(List.of(r1, r2, r3));

        ApiResponse response =
                caronaService.iniciarCarona(
                        carona.getId(),
                        "motorista"
                );

        assertTrue(response.isRetorno());

        Carona caronaAtualizada =
                caronaRepository.findById(carona.getId()).get();

        assertEquals(
                StatusCarona.EM_ANDAMENTO,
                caronaAtualizada.getStatusCarona()
        );

        List<ReservaCodigo> codigos =
                reservaCodigoRepository.findAll();

        assertEquals(3, codigos.size());

        Set<String> unicos = new HashSet<>();

        for (ReservaCodigo codigo : codigos) {

            assertTrue(
                    unicos.add(codigo.getCodigoEmbarque())
            );

            assertTrue(
                    unicos.add(codigo.getCodigoDesembarque())
            );
        }
    }

}