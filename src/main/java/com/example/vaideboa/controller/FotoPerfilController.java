package com.example.vaideboa.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.service.FotoPerfilService;
import com.example.vaideboa.service.FotoStorageService.Foto;

@RestController
@RequestMapping("/fotos")
public class FotoPerfilController {
    private final FotoPerfilService fotoPerfilService;

    public FotoPerfilController(FotoPerfilService fotoPerfilService) {
        this.fotoPerfilService = fotoPerfilService;
    }

    @PostMapping(value = "/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> cadastrarFotoPerfil(@RequestParam MultipartFile arquivo, Authentication auth) {
        ApiResponse retorno = fotoPerfilService.cadastrarFotoPerfil(auth.getName(), arquivo);
        if (!retorno.isRetorno()) {
            return ResponseEntity.badRequest().body(retorno.getMensagem());
        }
        return ResponseEntity.ok(retorno.getMensagem());
    }

    @GetMapping("/me")
    public ResponseEntity<?> buscarMinhaFoto(Authentication auth) {
        return respostaImagem(fotoPerfilService.buscarMinhaFoto(auth.getName()));
    }

    @GetMapping("/perfil/{idUsuario}")
    public ResponseEntity<?> buscarFotoPerfil(@PathVariable Long idUsuario,@RequestParam Long idCarona, Authentication auth) {
        return respostaImagem(fotoPerfilService.buscarFotoPerfil(auth.getName(), idUsuario, idCarona));
    }

    @GetMapping("/chat/{idReserva}")
    public ResponseEntity<?> buscarFotoChat(@PathVariable Long idReserva, Authentication auth) {
        return respostaImagem(fotoPerfilService.buscarFotoChat(auth.getName(), idReserva));
    }

    @GetMapping("/chat/{idReserva}/participante")
    public ResponseEntity<?> buscarParticipanteChat(@PathVariable Long idReserva,Authentication auth) {
        ApiResponse retorno = fotoPerfilService.buscarParticipanteChat(auth.getName(), idReserva);
        if (!retorno.isRetorno()) {
            return ResponseEntity.badRequest().body(retorno.getMensagem());
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(retorno.getDados());
    }

    @GetMapping("/avaliacao/{idAvaliacao}")
    public ResponseEntity<?> buscarFotoAvaliado(@PathVariable Long idAvaliacao, Authentication auth) {
        return respostaImagem(fotoPerfilService.buscarFotoAvaliado(auth.getName(), idAvaliacao));
    }

    private ResponseEntity<?> respostaImagem(ApiResponse retorno) {
        if (!retorno.isRetorno()) {
            return ResponseEntity.badRequest().body(retorno.getMensagem());
        }
        Foto foto = (Foto) retorno.getDados();
        return ResponseEntity.ok().contentType(foto.tipo()).cacheControl(CacheControl.noStore()).header("X-Content-Type-Options", "nosniff").body(foto.bytes());
    }
}
