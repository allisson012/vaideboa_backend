package com.example.vaideboa.validator;

import org.springframework.stereotype.Component;

@Component
public class SenhaValidator {
    public static boolean senhaValida(String senha) {
    if (senha == null || senha.isBlank()){
        return false;
    }

    if (senha.length() < 8) {
        return false;
    }

    if (!senha.matches(".*\\d.*")){
        return false;
    } 

    if (!senha.matches(".*[a-zA-Z].*")){
         return false;
    }

    if (!senha.matches(".*[^a-zA-Z0-9].*")){
         return false;
    }
    
    return true;
}
}
