package com.pixelwallet.util;

/** Formatação de valores em centavos no padrão brasileiro (R$ 1.234,56). */
public final class Dinheiro {

    /** Limite de dígitos digitados: R$ 999.999.999,99. */
    public static final int MAX_DIGITOS = 11;

    private Dinheiro() {
    }

    public static String formatar(long centavos) {
        boolean negativo = centavos < 0;
        // Math.abs(Long.MIN_VALUE) estoura; esse valor nunca aparece no app.
        long abs = Math.abs(centavos);
        long reais = abs / 100;
        long cents = abs % 100;

        String inteiro = Long.toString(reais);
        StringBuilder sb = new StringBuilder();
        int len = inteiro.length();
        for (int i = 0; i < len; i++) {
            sb.append(inteiro.charAt(i));
            int restantes = len - i - 1;
            if (restantes > 0 && restantes % 3 == 0) {
                sb.append('.');
            }
        }
        sb.append(',');
        if (cents < 10) {
            sb.append('0');
        }
        sb.append(cents);

        return (negativo ? "-R$ " : "R$ ") + sb;
    }

    /**
     * Converte o texto digitado em centavos usando só os dígitos, como numa
     * maquininha: "1250" → 1250 (R$ 12,50). Texto sem dígitos → 0.
     */
    public static long centavosDosDigitos(CharSequence texto) {
        long valor = 0;
        int digitos = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c >= '0' && c <= '9') {
                if (digitos == 0 && c == '0') {
                    continue; // ignora zeros à esquerda
                }
                if (digitos == MAX_DIGITOS) {
                    break;
                }
                valor = valor * 10 + (c - '0');
                digitos++;
            }
        }
        return valor;
    }
}
