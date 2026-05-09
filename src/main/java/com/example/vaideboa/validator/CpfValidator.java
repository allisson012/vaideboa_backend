package com.example.vaideboa.validator;

import org.springframework.stereotype.Component;

@Component
public class CpfValidator {

    public boolean cpfValido(String cpf) {
    if (cpf == null) return false;

    String cpfLimpo = cpf.replaceAll("[^\\d]", "");

    if (cpfLimpo.length() != 11) return false;

    if (cpfLimpo.chars().allMatch(c -> c == cpfLimpo.charAt(0))) return false;

    try {
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpfLimpo.charAt(i) - '0') * (10 - i);
        }
        int dig1 = 11 - (soma % 11);
        if (dig1 >= 10) dig1 = 0;

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpfLimpo.charAt(i) - '0') * (11 - i);
        }
        int dig2 = 11 - (soma % 11);
        if (dig2 >= 10) dig2 = 0;

        return dig1 == (cpfLimpo.charAt(9) - '0') &&
               dig2 == (cpfLimpo.charAt(10) - '0');

    } catch (Exception e) {
        return false;
    }
}
}
