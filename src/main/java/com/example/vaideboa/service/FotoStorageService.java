package com.example.vaideboa.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FotoStorageService {
    private static final Logger log = LoggerFactory.getLogger(FotoStorageService.class);
    private static final long TAMANHO_MAXIMO = 5 * 1024 * 1024;
    private static final long PIXELS_MAXIMOS = 12_000_000;
    private final Path pastaBase;

    public FotoStorageService(@Value("${app.fotos.diretorio}") String diretorio) {
        this.pastaBase = Path.of(diretorio).toAbsolutePath().normalize();
    }

    public String salvar(Long idUsuario, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "A foto deve ter no máximo 5 MB");
        }

        BufferedImage imagem;
        String formato;
        try (var entrada = arquivo.getInputStream(); ImageInputStream stream = ImageIO.createImageInputStream(entrada)) {
            if (stream == null) {
                throw imagemInvalida();
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw imagemInvalida();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                formato = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!formato.equals("jpeg") && !formato.equals("png")) {
                    throw imagemInvalida();
                }
                if ((long) reader.getWidth(0) * reader.getHeight(0) > PIXELS_MAXIMOS) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A foto deve ter no máximo 12 milhões de pixels");
                }
                imagem = reader.read(0);
                if (imagem == null) {
                    throw imagemInvalida();
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw imagemInvalida();
        }

        String referencia = "usuarios/usuario-" + idUsuario + "-" + UUID.randomUUID()
                + (formato.equals("jpeg") ? ".jpg" : ".png");
        Path destino = resolver(referencia);
        try {
            Files.createDirectories(destino.getParent());
            if (!ImageIO.write(imagem, formato, destino.toFile())) {
                throw new IOException("Não foi possível codificar a imagem");
            }
            return referencia;
        } catch (IOException e) {
            removerSeExistir(referencia);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar a foto", e);
        }
    }

    public Foto carregar(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário sem foto");
        }
        Path caminho = resolver(referencia);
        if (!Files.isRegularFile(caminho)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada");
        }
        try {
            MediaType tipo = referencia.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
            return new Foto(Files.readAllBytes(caminho), tipo);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível ler a foto", e);
        }
    }

    public void removerSeExistir(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolver(referencia));
        } catch (IOException | ResponseStatusException e) {
            log.warn("Não foi possível remover o arquivo de foto {}", referencia, e);
        }
    }

    private Path resolver(String referencia) {
        if (!referencia.matches("usuarios/usuario-\\d+-[0-9a-f-]{36}\\.(jpg|png)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Referência de foto inválida");
        }
        Path caminho = pastaBase.resolve(referencia).normalize();
        if (!caminho.startsWith(pastaBase)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Referência de foto inválida");
        }
        return caminho;
    }

    private ResponseStatusException imagemInvalida() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma imagem JPEG ou PNG válida");
    }

    public record Foto(byte[] bytes, MediaType tipo) {
    }
}
