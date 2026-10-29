package com.pixelwallet.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

/** Textos de data em maiúsculas e sem depender do idioma do celular. */
public final class Datas {

    private static final String[] MESES = {
            "JAN", "FEV", "MAR", "ABR", "MAI", "JUN",
            "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"};
    private static final String[] SEMANA = {"SEG", "TER", "QUA", "QUI", "SEX", "SAB", "DOM"};

    private Datas() {
    }

    public static LocalDateTime deMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    /** "OUT/2026" */
    public static String mes(YearMonth m) {
        return MESES[m.getMonthValue() - 1] + "/" + m.getYear();
    }

    /** "QUA 29/10" */
    public static String dia(LocalDate d) {
        return SEMANA[d.getDayOfWeek().getValue() - 1] + " " + doisDigitos(d.getDayOfMonth())
                + "/" + doisDigitos(d.getMonthValue());
    }

    /** "14:32" */
    public static String hora(long millis) {
        LocalDateTime t = deMillis(millis);
        return doisDigitos(t.getHour()) + ":" + doisDigitos(t.getMinute());
    }

    public static String doisDigitos(int n) {
        return n < 10 ? "0" + n : Integer.toString(n);
    }
}
