package com.example.vaideboa.service;

import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.PontoParadaRepository;
import com.example.vaideboa.repository.RotaRepository;
import com.example.vaideboa.repository.AvaliacaoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CaronaDto;
import com.example.vaideboa.Dtos.CaronaRetornoDto;
import com.example.vaideboa.Dtos.ParadaDto;
import com.example.vaideboa.Dtos.PontoParadaRetornoDto;
import com.example.vaideboa.Dtos.RotaInfoDto;
import com.example.vaideboa.Dtos.ViagemRealizadaDTO;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PontoParada;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.Rota;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.Avaliacao;
import com.example.vaideboa.model.enums.StatusCarona;
import com.example.vaideboa.model.enums.TipoAvaliacao;
import com.example.vaideboa.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class CaronaService {
    private final CaronaRepository caronaRepository;
    private final RotaRepository rotaRepository;
    private final UserRepository userRepository; 
    private final RotaService rotaService;
    private final AvaliacaoService avaliacaoService;
    private final GeoService geoService;
    private final CodigoService codigoService;
    private final PontoParadaRepository pontoParadaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final CompartilhamentoService compartilhamentoService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public CaronaService(CaronaRepository caronaRepository, RotaRepository rotaRepository,
        UserRepository userRepository, RotaService rotaService, AvaliacaoService avaliacaoService,
        GeoService geoService, CodigoService codigoService, PontoParadaRepository pontoParadaRepository,
        AvaliacaoRepository avaliacaoRepository, CompartilhamentoService compartilhamentoService) {
      this.caronaRepository = caronaRepository;
      this.rotaRepository = rotaRepository;
      this.userRepository = userRepository;
      this.rotaService = rotaService;
      this.avaliacaoService = avaliacaoService;
      this.geoService = geoService;
      this.codigoService = codigoService;
      this.pontoParadaRepository = pontoParadaRepository;
      this.avaliacaoRepository = avaliacaoRepository;
      this.compartilhamentoService = compartilhamentoService;
    }

    @Transactional
    public boolean cadastrarCarona(CaronaDto caronaDto , String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return false;
      }
      User user = userOpt.get();
      Rota rota = new Rota();
      Carona carona = new Carona();
      Point saida = geometryFactory.createPoint(
        new Coordinate(caronaDto.getSaidaLng(), caronaDto.getSaidaLat())
      );
      Point destino = geometryFactory.createPoint(
        new Coordinate(caronaDto.getDestinoLng(), caronaDto.getDestinoLat())
      );
      carona.setStatusCarona(StatusCarona.EM_ESPERA);
      rota.setSaida(saida);
      rota.setDestino(destino);

      List<PontoParada> pontosParadas = new ArrayList<>();
      String geojson;

      if(!caronaDto.getParadas().isEmpty()){
        List<ParadaDto> paradasOrdenadas = caronaDto.getParadas().stream()
          .sorted(Comparator.comparingInt(ParadaDto::getIndexOrder))
          .toList();
        // ordenando para garantir que vai seguir a order correta
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(Arrays.asList(saida.getX(), saida.getY()));
        for (ParadaDto parada : paradasOrdenadas){
          coordinates.add(Arrays.asList(parada.getLongitude(), parada.getLatitude()));
          PontoParada pontoParada = new PontoParada();
          pontoParada.setIndexOrder(parada.getIndexOrder());
          Point localizacao = geometryFactory.createPoint(
            new Coordinate(parada.getLongitude(), parada.getLatitude())
          );
          pontoParada.setTextoPonto(geoService.reverseGeocode(localizacao.getY(), localizacao.getX()));
          pontoParada.setLocalizacao(localizacao);
          pontoParada.setRota(rota);
          pontosParadas.add(pontoParada);
        }
        coordinates.add(Arrays.asList(destino.getX(), destino.getY()));
        geojson = rotaService.getRotaComParadas(coordinates);
      }else{
        geojson = rotaService.getRota(saida, destino);  
      }
      if(geojson == null || geojson.isBlank()) {
        return false;
      }
      LineString trajeto = rotaService.salvarRota(geojson);
      rota.setTrajeto(trajeto);
      RotaInfoDto rotaInfoDto = rotaService.extrairInfoRota(geojson);
      rota.setDistancia(rotaInfoDto.getDistanciaKm());
      rota.setDuracao(rotaInfoDto.getDuracaoMin());
      rota.setSaidaTexto(geoService.reverseGeocode(saida.getY(), saida.getX()));
      rota.setDestinoTexto(geoService.reverseGeocode(destino.getY(), destino.getX()));
      Rota rotaSalva = rotaRepository.save(rota);
      if(rotaSalva == null)
      {
        return false;
      }
      if(!pontosParadas.isEmpty()){
        pontoParadaRepository.saveAll(pontosParadas);
      }
      carona.setQntAssentos(caronaDto.getQntAssentos());
      carona.setVagasDisponiveis(caronaDto.getQntAssentos());
      carona.setMotorista(user);
      carona.setData(caronaDto.getData());
      carona.setHora(caronaDto.getHora());
      carona.setRota(rotaSalva);
      caronaRepository.save(carona);
      return true;
    }

    public ApiResponse iniciarCarona(Long idCarona, String username){
      User user = userRepository.findByUsernameAndAtivoTrue(username)
      .orElse(null);

      if (user == null) {
          return new ApiResponse(false, "Usuário não encontrado");
      }

      Carona carona = caronaRepository.findById(idCarona).orElse(null);
      if(carona == null){
        return new ApiResponse(false,"Carona não encontrada");
      }
      if(!carona.getMotorista().getId().equals(user.getId())){
        return new ApiResponse(false,"Usuário não tem acesso a essa carona");
      }
      if(!carona.getData().equals(LocalDate.now())){
        return new ApiResponse(false,"Carona fora da data agendada");
      }
      LocalTime agora = LocalTime.now();
      LocalTime horarioCarona = carona.getHora();

      LocalTime horaInferior = horarioCarona.minusMinutes(20);
      LocalTime horaSuperior = horarioCarona.plusMinutes(60);
      if(agora.isBefore(horaInferior)){
        return new ApiResponse(false,"Ainda não é possível iniciar a carona. Aguarde o horário permitido.");
      }
      if(agora.isAfter(horaSuperior)){
        return new ApiResponse(false,"Não é mais possível iniciar a carona. O horário limite foi excedido.");
      } 
      carona.setStatusCarona(StatusCarona.EM_ANDAMENTO);
      ApiResponse retorno = codigoService.gerarCodigos(carona.getReservas());
      if(!retorno.isRetorno()){
        return retorno;
      }
      caronaRepository.save(carona);
      compartilhamentoService.iniciarCompartilhamento(idCarona);
      return new ApiResponse(true, "Carona iniciada com sucesso");
    }

    public ApiResponse finalizarCorrida(Long idCarona, String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false, "Usuário não encontrado", null);
      }
      Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
      if(caronaOpt.isEmpty()){
        return new ApiResponse(false, "Carona não encontrada", null);
      }
      Carona carona = caronaOpt.get();
      User user = userOpt.get();
      if(carona.isRealizado()){
        return new ApiResponse(false, "Carona já realizada", null);
      }
      if(!carona.getMotorista().equals(user)){
        return new ApiResponse(false,"Usuário não tem acesso a carona pois não é o motorista", null);
      }
      carona.setRealizado(true);
      boolean sucesso = avaliacaoService.criarAvaliacoes(carona);
      if(!sucesso){
          return new ApiResponse(false, "Erro ao criar avaliações", null);
      }
      compartilhamentoService.finalizarCompartilhamento(idCarona);
      caronaRepository.save(carona); 
      return new ApiResponse(true, "Carona finalizada com sucesso", null);
    }

    public ApiResponse minhasViagens(String username, String tipo) {
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if (userOpt.isEmpty()) {
          return new ApiResponse(false, "Usuário não encontrado", null);
      }

      User user = userOpt.get();
      Set<Long> idsAdicionados = new HashSet<>();
      List<ViagemRealizadaDTO> resultado = new ArrayList<>();

      String filtro = (tipo == null) ? "TODAS" : tipo.toUpperCase();

      for (Carona c : user.getMinhasCaronas()) {

          if (c.getRota() == null) continue;

          boolean realizada = c.isRealizado();

          if(filtro.equals("REALIZADAS") && !realizada) continue;
          if(filtro.equals("AGENDADAS") && realizada) continue;

          Point origem = c.getRota().getSaida();
          Point destino = c.getRota().getDestino();

          resultado.add(new ViagemRealizadaDTO(
                  c.getId(),
                  origem.getY(),
                  origem.getX(),
                  destino.getY(),
                  destino.getX(),
                  c.getData(),
                  c.getHora(),
                  "MOTORISTA",
                  realizada,
                  c.getRota().getSaidaTexto(),
                  c.getRota().getDestinoTexto(),
                  null,
                  null,
                  converterParadas(c.getRota().getRota_points())
          ));
          idsAdicionados.add(c.getId());
      }
      for (Reserva r : user.getMinhasReservas()) {

          Carona c = r.getCarona();

          if (c == null || c.getRota() == null) continue;

          boolean realizada = c.isRealizado();

          if(filtro.equals("REALIZADAS") && !realizada) continue;
          if(filtro.equals("AGENDADAS") && realizada) continue;

          Point origem = c.getRota().getSaida();
          Point destino = c.getRota().getDestino();

          ViagemRealizadaDTO dto = new ViagemRealizadaDTO(
                  c.getId(),
                  origem.getY(),
                  origem.getX(),
                  destino.getY(),
                  destino.getX(),
                  c.getData(),
                  c.getHora(),                
                  "PASSAGEIRO",
                  realizada,
                  c.getRota().getSaidaTexto(),
                  c.getRota().getDestinoTexto(),
                  null,
                  null,
                  converterParadas(c.getRota().getRota_points())
          );
          dto.setIdReserva(r.getId());
          Optional<Avaliacao> avaliacaoOpt = avaliacaoRepository.findByReservaAndAvaliadoAndAvaliador(
              r, c.getMotorista(), user);
          if (avaliacaoOpt.isPresent()) {
              dto.setIdAvaliacao(avaliacaoOpt.get().getId());
          }
          resultado.add(dto);
          idsAdicionados.add(c.getId());
      }
      resultado.sort(Comparator
              .comparing(ViagemRealizadaDTO::getData)
              .thenComparing(ViagemRealizadaDTO::getHora));

      return new ApiResponse(true, "Viagens encontradas com sucesso", resultado);
    }
    private List<PontoParadaRetornoDto> converterParadas(List<PontoParada> pontos) {
      List<PontoParadaRetornoDto> paradas = new ArrayList<>();

      if (pontos == null) {
          return paradas;
      }

      for (PontoParada ponto : pontos) {
          PontoParadaRetornoDto dto = new PontoParadaRetornoDto();

          dto.setLatPonto(ponto.getLocalizacao().getY());
          dto.setLonPonto(ponto.getLocalizacao().getX());
          dto.setIndexOrder(ponto.getIndexOrder());
          dto.setTextoPonto(ponto.getTextoPonto());

          paradas.add(dto);
      }

      paradas.sort(
          Comparator.comparingInt(PontoParadaRetornoDto::getIndexOrder)
      );

      return paradas;
    }

    public ApiResponse buscarCaronaPeloId(String username , Long idCarona){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false, "Usuário não encontrado");
      }
      User user = userOpt.get();
      Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
      if(caronaOpt.isEmpty()){
        return new ApiResponse(false, "Carona não encontrada");
      }
      Carona carona = caronaOpt.get();
      CaronaRetornoDto dto = new CaronaRetornoDto();
      dto.setData(carona.getData().toString());
      dto.setHora(carona.getHora().toString());
      dto.setQntAssentos(carona.getQntAssentos());
      dto.setVagasDisponiveis(carona.getVagasDisponiveis());
      dto.setRealizado(carona.isRealizado());
      dto.setLatSaida(carona.getRota().getSaida().getY());
      dto.setLonSaida(carona.getRota().getSaida().getX());
      dto.setSaidaTexto(carona.getRota().getSaidaTexto());
      dto.setLatDestino(carona.getRota().getDestino().getY());
      dto.setLonDestino(carona.getRota().getDestino().getX());
      dto.setDestinoTexto(carona.getRota().getDestinoTexto());
      dto.setDistancia(carona.getRota().getDistancia());
      dto.setDuracao(carona.getRota().getDuracao());
      dto.setNome(carona.getMotorista().getNome());
      dto.setGenero(carona.getMotorista().getGenero().getDescricao());
      dto.setIdRota(carona.getRota().getId());
      List<PontoParadaRetornoDto> paradasDto = new ArrayList<PontoParadaRetornoDto>();
      for (PontoParada parada : carona.getRota().getRota_points()) {
        PontoParadaRetornoDto paradaDto = new PontoParadaRetornoDto();
        paradaDto.setIndexOrder(parada.getIndexOrder());
        paradaDto.setLatPonto(parada.getLocalizacao().getY());
        paradaDto.setLonPonto(parada.getLocalizacao().getX());
        paradaDto.setTextoPonto(parada.getTextoPonto());
        paradasDto.add(paradaDto);
      }
      dto.setParadas(paradasDto);

      return new ApiResponse(true, "Busca feita com sucesso", dto);
    }

}
