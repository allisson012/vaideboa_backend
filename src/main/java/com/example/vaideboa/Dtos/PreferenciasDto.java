package com.example.vaideboa.Dtos;


public class PreferenciasDto {
    private final String conversa;
    private final String musica;
    private final String cigarro;
    private final String animais;
    
    public PreferenciasDto(String conversa, String musica, String cigarro, String animais) {
        this.conversa = conversa;
        this.musica = musica;
        this.cigarro = cigarro;
        this.animais = animais;
    }

    public String getConversa() {
        return conversa;
    }
    public String getMusica() {
        return musica;
    }
    public String getCigarro() {
        return cigarro;
    }
    public String getAnimais() {
        return animais;
    }
}
