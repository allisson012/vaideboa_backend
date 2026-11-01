package com.example.vaideboa.validator;

import org.springframework.stereotype.Component;

@Component
public class EmailValidator {

    public static boolean emailValido(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    }
}
